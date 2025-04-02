package com.lordnoisy.hoobabot.registry;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class ApplicationCommandRegistry {

    public static Mono<Void> registerApplicationCommands (GatewayDiscordClient gateway) {
        return createGlobalCommandsMono(createApplicationCommandRequests(), gateway);
    }

    public static List<ApplicationCommandRequest> createApplicationCommandRequests () {
        List<ApplicationCommandRequest> applicationCommandRequests = new ArrayList<>();

        //Create commands

        ImmutableApplicationCommandRequest.Builder pollCommandBuilder = ApplicationCommandRequest.builder()
                .name("poll")
                .description("Create a poll that everyone can vote on!")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("question")
                        .description("What question would you like to ask?")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .maxLength(255)
                        .required(true)
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("option_1")
                        .description("Add a custom option to your poll")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .maxLength(40)
                        .required(false)
                        .build());

        for (int i = 0; i < 19; i++) {
            pollCommandBuilder.addOption(ApplicationCommandOptionData.builder()
                    .name("option_"+(i+2))
                    .description("Add a custom option to your poll")
                    .type(ApplicationCommandOption.Type.STRING.getValue())
                    .maxLength(40)
                    .required(false)
                    .build());
        }

        pollCommandBuilder.addOption(ApplicationCommandOptionData.builder()
                .name("description")
                .description("Optionally add a longer description to your poll.")
                .type(ApplicationCommandOption.Type.STRING.getValue())
                .maxLength(255)
                .required(false)
                .build());

        pollCommandBuilder.addOption(ApplicationCommandOptionData.builder()
                .name("image")
                .description("Optionally include an image to be included in your poll")
                .type(ApplicationCommandOption.Type.ATTACHMENT.getValue())
                .required(false)
                .build());

        pollCommandBuilder.addOption(ApplicationCommandOptionData.builder()
                .name("open_poll")
                .description("Setting this to true will allow user submitted responses to your poll")
                .type(ApplicationCommandOption.Type.BOOLEAN.getValue())
                .required(false)
                .build());

        pollCommandBuilder.addOption(ApplicationCommandOptionData.builder()
                .name("empty_poll")
                .description("This allows there to be no options in your poll, and only to have user submitted ones")
                .type(ApplicationCommandOption.Type.BOOLEAN.getValue())
                .required(false)
                .build());
        ApplicationCommandRequest pollCommand = pollCommandBuilder.build();
        applicationCommandRequests.add(pollCommand);

        ApplicationCommandRequest datePollCommand = ApplicationCommandRequest.builder()
                .name("poll_dates")
                .description("Create a date poll that everyone can vote on!")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("question")
                        .description("What question would you like to ask?")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .maxLength(255)
                        .required(true)
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("start_date")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .description("The date you want your poll to start from. Format YYYY-MM-DD")
                        .maxLength(10)
                        .required(false)
                        .build()
                )
                .addOption(ApplicationCommandOptionData.builder()
                        .name("number_of_days")
                        .type(ApplicationCommandOption.Type.INTEGER.getValue())
                        .description("The number of options you want, e.g. '5' will give you 5 days into the future. Max 20")
                        .maxLength(2)
                        .required(false)
                        .build()
                )
                .addOption(ApplicationCommandOptionData.builder()
                        .name("interval")
                        .type(ApplicationCommandOption.Type.INTEGER.getValue())
                        .description("How many days you want between each date (e.g. 7 for weekly)")
                        .maxLength(4)
                        .required(false)
                        .build()
                )
                .addOption(ApplicationCommandOptionData.builder()
                        .name("description")
                        .description("Optionally add a longer description to your poll.")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .maxLength(255)
                        .required(false)
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                                .name("image")
                                .description("Optionally include an image to be included in your poll")
                                .type(ApplicationCommandOption.Type.ATTACHMENT.getValue())
                                .required(false)
                                .build()
                )
                .build();
        applicationCommandRequests.add(datePollCommand);

        ApplicationCommandRequest uptimeCommand = ApplicationCommandRequest.builder()
                .name("uptime")
                .description("Get the current uptime of the bot")
                .build();
        applicationCommandRequests.add(uptimeCommand);

        ApplicationCommandRequest binConfigureCommand = ApplicationCommandRequest.builder()
                .name("bin_config")
                .description("Configure bin reminders")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("bin_channel")
                        .description("Set the channel where bin reminders will go")
                        .type(ApplicationCommandOption.Type.CHANNEL.getValue())
                        .channelTypes(List.of(0))
                        .required(true)
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("delete_config")
                        .description("Set this to 'True' to disable bin reminders")
                        .type(ApplicationCommandOption.Type.BOOLEAN.getValue())
                        .required(false)
                        .build())
                .build();
        applicationCommandRequests.add(binConfigureCommand);

        ApplicationCommandRequest ticTacToeCommand = ApplicationCommandRequest.builder()
                .name("tic_tac_toe")
                .description("Challenge someone to tic tac toe")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("opponent")
                        .description("Pick a worthy competitor")
                        .type(ApplicationCommandOption.Type.USER.getValue())
                        .required(true)
                        .build())
                .build();
        applicationCommandRequests.add(ticTacToeCommand);

        ApplicationCommandRequest checkersCommand = ApplicationCommandRequest.builder()
                .name("checkers")
                .description("Challenge someone to checkers")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("opponent")
                        .description("Pick a worthy competitor")
                        .type(ApplicationCommandOption.Type.USER.getValue())
                        .required(true)
                        .build())
                .build();
        applicationCommandRequests.add(checkersCommand);

        ApplicationCommandRequest foxifyCommand = ApplicationCommandRequest.builder()
                .name("foxify")
                .description("New message format just dropped")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("message")
                        .description("Write your message")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("encoding")
                        .description("Custom encoding")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(false)
                        .build())
                .build();
        applicationCommandRequests.add(foxifyCommand);

        ApplicationCommandRequest helpCommand = ApplicationCommandRequest.builder()
                .name("help")
                .description("Find out how to use MONK-E!")
                .build();
        applicationCommandRequests.add(helpCommand);

        ApplicationCommandRequest binCommand = ApplicationCommandRequest.builder()
                .name("bins")
                .description("Find out what bin week it is in the SA1 area!")
                .build();
        applicationCommandRequests.add(binCommand);

        ApplicationCommandRequest videoCommand = ApplicationCommandRequest.builder()
                .name("video")
                .description("Search for a YouTube video")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("search")
                        .description("The search you would like to make.")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .build())
                .build();
        applicationCommandRequests.add(videoCommand);

        ApplicationCommandRequest ocrCommand = ApplicationCommandRequest.builder()
                .name("ocr_image")
                .description("Read text from an image")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("image_url")
                        .description("The URL of the image you would like to read.")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .build())
                .build();
        applicationCommandRequests.add(ocrCommand);

        ApplicationCommandRequest testCommand = ApplicationCommandRequest.builder()
                .name("test")
                .description("Developer tool to test WIP features")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("feature")
                        .description("The feature to test.")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .build())
                .build();
        applicationCommandRequests.add(testCommand);

        return applicationCommandRequests;
    }

    public static Mono<Void> createGlobalCommandsMono(List<ApplicationCommandRequest> applicationCommandRequestList, GatewayDiscordClient gateway) {
        Mono<Void> commandsMono = Mono.empty();
        for (ApplicationCommandRequest current : applicationCommandRequestList) {
            Mono<Void> createGlobalApplicationCommand = gateway.getRestClient().getApplicationId().flatMap(applicationID -> gateway.getRestClient().getApplicationService().createGlobalApplicationCommand(applicationID, current).then());
            commandsMono = commandsMono.and(createGlobalApplicationCommand);
        }
        return commandsMono;
    }
}
