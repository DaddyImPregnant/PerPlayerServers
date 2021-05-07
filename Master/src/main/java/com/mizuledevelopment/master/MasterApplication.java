package com.mizuledevelopment.master;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.mizuledevelopment.master.commands.*;
import com.mizuledevelopment.master.commands.rcon.AddCommand;
import com.mizuledevelopment.master.commands.rcon.ConnectCommand;
import com.mizuledevelopment.master.jedis.JedisManager;
import com.mizuledevelopment.master.jedis.JedisPublisher;
import com.mizuledevelopment.master.manager.NodeManager;
import com.mizuledevelopment.master.mongo.Mongo;
import com.mizuledevelopment.master.threads.CleanupThread;
import io.github.revxrsal.cub.cli.core.CLIHandler;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Properties;
import java.util.Scanner;

public class MasterApplication {

    @Getter private static CLIHandler cliHandler;
    @Getter private static NodeManager nodeManager;
    @Getter private static JedisManager jedisManager;
    @Getter private static String rconPassword; //TODO remove
    @Getter private static DockerClient dockerClient;
    @Getter private static Properties config;

    @SneakyThrows
    public static void main(String[] args) {
        copyResource();

        if (!new File("config.properties").exists()) {
            System.out.println("Config not found. Starting will fail.");
        }

        config = new Properties();
        config.load(new FileInputStream("config.properties"));

        rconPassword = config.getProperty("rcon.password");

        DockerClientConfig clientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder().withDockerHost(config.getProperty("docker.host")).build();
        dockerClient = DockerClientBuilder.getInstance(clientConfig).build();

        new Mongo().connectMongo();

        nodeManager = new NodeManager();

        jedisManager = new JedisManager(config.getProperty("redis.host"), Integer.parseInt(config.getProperty("redis.port")), "Testing-Master", System.getProperty("redis.password"));


        Scanner scanner = new Scanner(System.in);
        cliHandler = new CLIHandler(scanner, new PrintStream(System.out));

        Arrays.asList(
                new ConnectCommand(),
                new FindCommand(),
                new AddCommand(),
                new HelpCommand(),
                new CreateCommand(),
                new StartCommand(),
                new StopCommand(),
                new DebugCommand(),
                new RemoveCommand()
        ).forEach(cliHandler::registerCommand);

        new CleanupThread().cleanup();

        cliHandler.requestInput();

    }
    
    @SneakyThrows
    public static void copyResource() {
        URL inputUrl = MasterApplication.class.getResource("/config.properties");
        File dest = new File("config.properties");
        FileUtils.copyURLToFile(inputUrl, dest);
    }
}
