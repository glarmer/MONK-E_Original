package com.lordnoisy.hoobabot;

import com.lordnoisy.hoobabot.utility.DiscordUtilities;
import com.lordnoisy.hoobabot.utility.EmbedBuilder;
import com.lordnoisy.hoobabot.utility.Utilities;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.Embed;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.component.*;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.Reaction;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.*;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Poll {
    private static final String BAR_EMOJI_ONE = "<:1_:902952221761753148>";
    private static final String BAR_EMOJI_TWO = "<:2_:902952173674057738>";
    private static final String BAR_EMOJI_THREE = "<:3_:902952173644677170>";
    private static final String BAR_EMOJI_FOUR = "<:4_:902952173560815676>";
    private static final String BAR_EMOJI_FIVE = "<:5_:902952173531431012>";
    private static final String BAR_EMOJI_SIX = "<:6_:902952174986862622>";
    private static final String BAR_EMOJI_SEVEN = "<:7_:902952173489508383>";
    private static final String BAR_EMOJI_EIGHT = "<:8_:902952173741166632>";
    private static final String BAR_EMOJI_NINE = "<:9_:902952173623705640>";

    private static final String POLL_OPTION_ONE = "\u0031\u20E3";
    private static final String POLL_OPTION_TWO = "\u0032\u20E3";
    private static final String POLL_OPTION_THREE = "\u0033\u20E3";
    private static final String POLL_OPTION_FOUR = "\u0034\u20E3";
    private static final String POLL_OPTION_FIVE = "\u0035\u20E3";
    private static final String POLL_OPTION_SIX = "\u0036\u20E3";
    private static final String POLL_OPTION_SEVEN = "\u0037\u20E3";
    private static final String POLL_OPTION_EIGHT = "\u0038\u20E3";
    private static final String POLL_OPTION_NINE = "\u0039\u20E3";
    private static final String POLL_OPTION_TEN = "\u0030\u20E3";
    private static final String POLL_OPTION_ELEVEN = "\ud83c\udde6";
    private static final String POLL_OPTION_TWELVE = "\ud83c\udde7";
    private static final String POLL_OPTION_THIRTEEN = "\ud83c\udde8";
    private static final String POLL_OPTION_FOURTEEN = "\ud83c\udde9";
    private static final String POLL_OPTION_FIFTEEN = "\ud83c\uddea";
    private static final String POLL_OPTION_SIXTEEN = "\ud83c\uddeb";
    private static final String POLL_OPTION_SEVENTEEN = "\ud83c\uddec";
    private static final String POLL_OPTION_EIGHTEEN = "\ud83c\udded";
    private static final String POLL_OPTION_NINETEEN = "\ud83c\uddee";
    private static final String POLL_OPTION_TWENTY = "\ud83c\uddef";

    private static final String[] POLL_REACTIONS = new String[] { POLL_OPTION_ONE, POLL_OPTION_TWO,
            POLL_OPTION_THREE, POLL_OPTION_FOUR, POLL_OPTION_FIVE, POLL_OPTION_SIX, POLL_OPTION_SEVEN, POLL_OPTION_EIGHT, POLL_OPTION_NINE,
            POLL_OPTION_TEN, POLL_OPTION_ELEVEN, POLL_OPTION_TWELVE, POLL_OPTION_THIRTEEN, POLL_OPTION_FOURTEEN, POLL_OPTION_FIFTEEN,
            POLL_OPTION_SIXTEEN, POLL_OPTION_SEVENTEEN, POLL_OPTION_EIGHTEEN, POLL_OPTION_NINETEEN, POLL_OPTION_TWENTY};
    private static final List<String> reactsList = List.of(POLL_REACTIONS);
    private static final String DELETE_CROSS_REACT = "\u274c";
    private static final int MAX_NUMBER_OF_OPTIONS = 20;
    private final EmbedBuilder embeds;

    public Poll(EmbedBuilder embeds){
        this.embeds = embeds;
    }

    /**
     * Create the bars of emojis that represent poll votes
     * @param responses the number of responses to the poll for each option
     * @return an array of bars
     */
    public ArrayList<String> calculateEmotes(int[] responses) {
        //Most responses instead of total, since this is relative
        double mostResponses = 0;
        ArrayList<String> responseEmojis = new ArrayList<>();

        for (int i = 0; i < responses.length; i++) {
            if (mostResponses < responses[i]) {
                mostResponses = responses[i];
            }
        }
        if (mostResponses < 1){
            mostResponses++;
        }

        for (int i = 0; i < responses.length; i++) {
            //Change mostResponses to totalResponses to get everything relative to 100% instead of relative to the highest responded option
            double percentage = ((responses[i] / mostResponses) * 100);

            String emojis = "|";
            int iterations = 0;
            //TODO: There has to be a better way...
            while (percentage >= 0 && iterations < 8) {
                if (percentage >= 12.5) {
                    emojis = emojis + BAR_EMOJI_NINE;
                    percentage = percentage - 12.5;
                } else if (percentage >= 10.9375) {
                    emojis = emojis + BAR_EMOJI_EIGHT;
                    percentage = percentage - 10.9375;
                } else if (percentage >= 9.375) {
                    emojis = emojis + BAR_EMOJI_SEVEN;
                    percentage = percentage - 9.375;
                } else if (percentage >= 7.8125) {
                    emojis = emojis + BAR_EMOJI_SIX;
                    percentage = percentage - 7.8125;
                } else if (percentage >= 6.25) {
                    emojis = emojis + BAR_EMOJI_FIVE;
                    percentage = percentage - 6.25;
                } else if (percentage >= 4.6875) {
                    emojis = emojis + BAR_EMOJI_FOUR;
                    percentage = percentage - 4.6875;
                } else if (percentage >= 3.125) {
                    emojis = emojis + BAR_EMOJI_THREE;
                    percentage = percentage - 3.125;
                } else if (percentage >= 1.5625) {
                    emojis = emojis + BAR_EMOJI_TWO;
                    percentage = percentage - 1.5625;
                } else {
                    iterations++;
                    int needed = 8 - iterations;
                    for (int j = 0; j <= needed; j++) {
                        emojis = emojis + BAR_EMOJI_ONE;
                    }
                    break;
                }
                iterations++;
            }
            responseEmojis.add(emojis);
        }
        return responseEmojis;
    }

    /**
     * Get the number of responses of each option
     * @param numberOfOptions how many options the poll has
     * @param reactions the reactions from the poll message
     * @return an array of the responses in order
     */
    public int[] getResponses(int numberOfOptions, List<Reaction> reactions) {
        int[] responses = new int[numberOfOptions];


        for (Reaction currentReaction : reactions) {
            String currentName;

            //TODO: Consider deleting the emoji earlier on, we dont want random non-poll emotes on polls anyway
            if (currentReaction.getEmoji().asUnicodeEmoji().isPresent()) {
                currentName = currentReaction.getEmoji().asUnicodeEmoji().get().getRaw();
            } else {
                break;
            }

            try {
                //TODO: There has to be a better way of doing this

                switch (currentName) {
                    case POLL_OPTION_ONE:
                        responses[0] = currentReaction.getCount() - 1;
                        break;
                    case POLL_OPTION_TWO:
                        if (responses.length > 1) {
                            responses[1] = currentReaction.getCount() - 1;
                        }
                        break;
                    case POLL_OPTION_THREE:
                        if (responses.length > 2) {
                            responses[2] = currentReaction.getCount() - 1;
                        }
                        break;
                    case POLL_OPTION_FOUR:
                        if (responses.length > 3) {
                            responses[3] = currentReaction.getCount() - 1;
                        }
                        break;
                    case POLL_OPTION_FIVE:
                        if (responses.length > 4) {
                            responses[4] = currentReaction.getCount() - 1;
                        }
                        break;
                    case POLL_OPTION_SIX:
                        if (responses.length > 5) {
                            responses[5] = currentReaction.getCount() - 1;
                        }
                        break;
                    case POLL_OPTION_SEVEN:
                        if (responses.length > 6) {
                            responses[6] = currentReaction.getCount() - 1;
                        }
                        break;
                    case POLL_OPTION_EIGHT:
                        if (responses.length > 7) {
                            responses[7] = currentReaction.getCount() - 1;
                        }
                        break;
                    case POLL_OPTION_NINE:
                        if (responses.length > 8) {
                            responses[8] = currentReaction.getCount() - 1;
                        }
                        break;
                    case POLL_OPTION_TEN:
                        if (responses.length > 9) {
                            responses[9] = currentReaction.getCount() - 1;
                        }
                        break;
                    case POLL_OPTION_ELEVEN:
                        if (responses.length > 10) {
                            responses[10] = currentReaction.getCount() - 1;
                        }
                        break;
                    case POLL_OPTION_TWELVE:
                        if (responses.length > 11) {
                            responses[11] = currentReaction.getCount() - 1;
                        }
                        break;
                    case POLL_OPTION_THIRTEEN:
                        if (responses.length > 12) {
                            responses[12] = currentReaction.getCount() - 1;
                        }
                        break;
                    case POLL_OPTION_FOURTEEN:
                        if (responses.length > 13) {
                            responses[13] = currentReaction.getCount() - 1;
                        }
                        break;
                    case POLL_OPTION_FIFTEEN:
                        if (responses.length > 14) {
                            responses[14] = currentReaction.getCount() - 1;
                        }
                        break;
                    case POLL_OPTION_SIXTEEN:
                        if (responses.length > 15) {
                            responses[15] = currentReaction.getCount() - 1;
                        }
                        break;
                    case POLL_OPTION_SEVENTEEN:
                        if (responses.length > 16) {
                            responses[16] = currentReaction.getCount() - 1;
                        }
                        break;
                    case POLL_OPTION_EIGHTEEN:
                        if (responses.length > 17) {
                            responses[17] = currentReaction.getCount() - 1;
                        }
                        break;
                    case POLL_OPTION_NINETEEN:
                        if (responses.length > 18) {
                            responses[18] = currentReaction.getCount() - 1;
                        }
                        break;
                    case POLL_OPTION_TWENTY:
                        if (responses.length > 19) {
                            responses[19] = currentReaction.getCount() - 1;
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return responses;
    }

    public Mono<Void> processDatePollCommand(ChatInputInteractionEvent event) {
        String question = "";
        String description = "";
        Attachment attachment = null;
        String startDate = "";
        int interval = 1;
        int numberOfDays = 20;
        for (int i = 0; i < event.getOptions().size(); i++) {
            ApplicationCommandInteractionOption option = event.getOptions().get(i);
            String optionName = option.getName();
            switch (optionName) {
                case "question" -> question = option.getValue().get().asString();
                case "description" -> description = option.getValue().get().asString();
                case "image" -> {
                    String attachmentRaw = option.getValue().get().getRaw();
                    Snowflake attachmentSnowflake = Snowflake.of(attachmentRaw);
                    attachment = event.getInteraction().getCommandInteraction().get().getResolved().get().getAttachment(attachmentSnowflake).get();
                }
                case "start_date" -> startDate = option.getValue().get().asString();
                case "interval" -> interval = (int) option.getValue().get().asLong();
                case "number_of_days" -> numberOfDays = (int) option.getValue().get().asLong();
            }
        }
        List<Attachment> attachments = null;
        if (attachment != null) {
            attachments = List.of(attachment);
        }


        Mono<MessageChannel> channelMono = event.getInteraction().getChannel();
        if (event.getInteraction().getMember().isPresent()) {
            Member member = event.getInteraction().getMember().get();
            return this.createDatePoll(member, question, description, attachments, channelMono, numberOfDays, startDate, interval);
        } else {
            return channelMono
                    .ofType(MessageChannel.class)
                    .flatMap(messageChannel -> messageChannel.createMessage(getPollDMsEmbed())
                    ).then();
        }
    }

    public Mono<Void> processPollCommand(ChatInputInteractionEvent event) {
        String question = "";
        String description = "";
        Attachment attachment = null;
        ArrayList<String> options = new ArrayList<>();
        boolean isOpenPoll = false;
        boolean hasOptions = true;

        for (int i = 0; i < event.getOptions().size(); i++) {
            ApplicationCommandInteractionOption option = event.getOptions().get(i);
            String optionName = option.getName();
            if (optionName.startsWith("option")) {
                options.add(option.getValue().get().asString().concat(":"));
            }
            switch (optionName) {
                case "question" -> question = option.getValue().get().asString();
                case "description" -> description = option.getValue().get().asString();
                case "image" -> {
                    String attachmentRaw = option.getValue().get().getRaw();
                    Snowflake attachmentSnowflake = Snowflake.of(attachmentRaw);
                    attachment = event.getInteraction().getCommandInteraction().get().getResolved().get().getAttachment(attachmentSnowflake).get();
                }
                case "open_poll" ->
                        isOpenPoll = event.getOption("open_poll").get().getValue().get().asBoolean();
                case "empty_poll" ->
                        hasOptions = !event.getOption("empty_poll").get().getValue().get().asBoolean();
            }
        }

        List<Attachment> attachments = null;
        if (attachment != null) {
            attachments = List.of(attachment);
        }

        if (event.getInteraction().getMember().isPresent()) {
            Member member = event.getInteraction().getMember().get();
            return this.createPoll(member, options, question, description, attachments, event.getInteraction().getChannel(), isOpenPoll, hasOptions);
        } else {
            return event.getInteraction().getChannel()
                    .ofType(MessageChannel.class)
                    .flatMap(messageChannel -> messageChannel.createMessage(getPollDMsEmbed())
                    ).then();
        }
    }

    /**
     * Generate dates (based off of input) to fill a poll
     * @param numberOfDays the number of dates to generate
     * @param startDate the date of the first date
     * @param interval the number of days between each date (e.g. 7 creates weekly options)
     * @return the list of generated dates
     */
    public ArrayList<String> generateDateOptions(int numberOfDays, String startDate, int interval) {
        ArrayList<String> options = new ArrayList<>();
        if (numberOfDays > MAX_NUMBER_OF_OPTIONS || numberOfDays <= 0) {
            //Lazy :)
            numberOfDays = MAX_NUMBER_OF_OPTIONS;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate.now(ZoneId.of("Europe/London"));
        LocalDate localDate = LocalDate.parse(LocalDate.now(ZoneId.of("Europe/London")).format(formatter), formatter);
        if (startDate != null) {
            try {
                localDate = LocalDate.parse(startDate, formatter);
            } catch (DateTimeParseException dateTimeParseException) {
                localDate = LocalDate.parse(LocalDate.now(ZoneId.of("Europe/London")).format(formatter), formatter);
            }

        }
        String nowDate = localDate.format(formatter).toString();

        for (int i = 0; i < numberOfDays * interval; i = i + interval) {
            String day = localDate.plusDays(i).getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault());
            String month = localDate.plusDays(i).getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault());
            options.add(day + " " + localDate.plusDays(i).getDayOfMonth() + " " + month);
        }

        String finalDate = localDate.plusDays((numberOfDays - 1) * (interval )).format(formatter).toString();

        //This is a horrible solution
        options.add("\n**Date Range:** *" + nowDate + " to " + finalDate + "*");
        return options;
    }


    /**
     * Creates a date poll
     * @param member the member who created the poll
     * @param question the question of the poll
     * @param description the description of the poll
     * @param attachments any attachments of the poll
     * @param channelMono the message channel mono
     * @param numberOfDays the number of dates the poll should contain
     * @param startDate the starting date of the poll
     * @param interval the time in days between each date in the poll (inclusive)
     * @return the finished poll mono
     */
    public Mono<Void> createDatePoll(Member member, String question, String description, List<Attachment> attachments, Mono<MessageChannel> channelMono, int numberOfDays, String startDate, int interval) {
        ArrayList<String> options = generateDateOptions(numberOfDays, startDate, interval);
        String finalDate = options.get(options.size() - 1);
        options.remove(options.size() - 1);
        if (description == null) {
            description = "";
        } else {
            description = description + "\n";
        }
        description = description + finalDate;
        return createPoll(member, options , question, description, attachments, channelMono, false, true);
    }

    /**
     * Create a poll
     * @param member the member who created the poll
     * @param attachments a list of attachments
     * @param channelMono the message channel mono
     * @return a mono that creates a poll
     */
    public Mono<Void> createPoll(Member member, ArrayList<String> options, String question, String description, List<Attachment> attachments, Mono<MessageChannel> channelMono, boolean isOpenPoll, boolean hasOptions) {
        //Easy check to see if this is being done in DMs, and act accordingly
        if (member != null) {
            String username = member.getDisplayName();
            String title = username.concat(" has started a poll!");
            String profileImgURL = member.getAvatarUrl();
            String attachedUrl = getImageUrl(attachments);

            if (options.size() == 0 && hasOptions) {
                options.add("Yes:");
                options.add("No:");
                options.add("Maybe:");
            } else if (!hasOptions) {
                isOpenPoll = true;
            }

            //Prepare empty response emojis
            int[] responses = new int[options.size()];
            for (int i = 0; i < options.size(); i++) {
                responses[i] = 0;
            }


            ArrayList<String> emojis = calculateEmotes(responses);

            EmbedCreateSpec pollEmbed = embeds.createPollEmbed(title, description, profileImgURL, question, emojis, attachedUrl, options);

            MessageCreateSpec messageCreateSpec = MessageCreateSpec.builder()
                    .addEmbed(pollEmbed)
                    .build();

            Button deleteButton = Button.danger("delete:"+member.getId().asString(), "X");
            Button endPollButton = Button.primary("poll_end_poll:"+member.getId().asString(), "End the poll");
            if (isOpenPoll) {
                Button button = Button.primary("poll:add_option", "Add a poll option...");
                messageCreateSpec = messageCreateSpec.withComponents(ActionRow.of(button, endPollButton, deleteButton));

            } else {
                messageCreateSpec = messageCreateSpec.withComponents(ActionRow.of(endPollButton, deleteButton));
            }

            final MessageCreateSpec MESSAGE_CREATE_SPEC = messageCreateSpec;
            ArrayList<String> finalOptions = options;
            return channelMono
                    .ofType(MessageChannel.class)
                    .flatMap(messageChannel -> messageChannel.createMessage(MESSAGE_CREATE_SPEC)
                            .flatMap(message -> {
                                if (hasOptions) {
                                    return addPollReacts(message, finalOptions);
                                } else {
                                    return Mono.empty();
                                }
                            })
                    ).then();
        } else {
            return channelMono
                    .ofType(MessageChannel.class)
                    .flatMap(messageChannel -> messageChannel.createMessage(getPollDMsEmbed())
                    ).then();
        }
    }

    public InteractionApplicationCommandCallbackReplyMono createEndPollSelectMenu(ButtonInteractionEvent event, String id) {
        ArrayList<SelectMenu.Option> options = new ArrayList<>();
        options.add(SelectMenu.Option.of("Yes", id+":yes"));
        options.add(SelectMenu.Option.of("No", id+":no"));
        SelectMenu selectMenu = SelectMenu.of(id, options);

        return event.reply("Would you like to notify the voters?").withComponents(ActionRow.of(selectMenu)).withEphemeral(true);
    }



    public EmbedCreateSpec getPollDMsEmbed(){
        return EmbedCreateSpec.builder()
                .color(EmbedBuilder.getStandardColor())
                .title("Monkey see, monkey poll?")
                .description("You can't make polls in DMs, silly! To make a poll use the poll command in a server!")
                .timestamp(Instant.now())
                .footer(EmbedBuilder.getFooterText(), (EmbedBuilder.getFooterIconUrl() + Utilities.getRandomNumber(0,156) + ".png"))
                .build();
    }

    /**
     * Add reaction emotes to a poll message
     * @return the emotes mono
     */
    public Mono<Void> addPollReacts(Message message, ArrayList<String> optionsArray) {
        Mono<Void> addEmotes = Mono.empty();
        try {
            for (int i = 0; i < optionsArray.size() && i < MAX_NUMBER_OF_OPTIONS; i++) {
                addEmotes = addEmotes.and(message.addReaction(ReactionEmoji.unicode(POLL_REACTIONS[i])));
            }
        } catch (Exception e) {
            e.printStackTrace();
            addEmotes = Mono.empty();
        }
        return addEmotes;
    }

    /**
     * Find if an image is attached to the Poll creation message, and get the URL of the attachment
     * @param attachments list of the messages attachments
     * @return URL of image attachment
     */
    public String getImageUrl(List<Attachment> attachments) {
        String attachedUrl = "";
        if(!(attachments == null)) {
            for (int i = 0; i < attachments.size(); i++) {
                if(attachments.get(i).getContentType().get().contains("image")) {
                    attachedUrl = attachments.get(i).getUrl();
                    break;
                }
            }
        }
        return attachedUrl;
    }

    /**
     * Update the poll to represent vote distribution
     * @param pollMessage the original poll message
     * @return a Mono to update the poll
     */
    public Mono<Object> updatePoll(Mono<Message> pollMessage, String newOption) {
        return pollMessage.flatMap(message -> {
            //Check that the message was sent by the bot
            if (!DiscordUtilities.isBotMessage(message.getClient(), message)) {
                return Mono.empty();
            }

            //Check that there is an embed
            if (!(message.getEmbeds().size() > 0)) {
                return Mono.empty();
            }

            Embed pollEmbed = message.getEmbeds().get(0);

            //Check that there are fields
            if (pollEmbed.getFields().size() < 2) {
                return Mono.empty();
            }

            Embed.Field optionsField = pollEmbed.getFields().get(0);
            Embed.Field responsesField = pollEmbed.getFields().get(1);

            //Check that the embed is definitely a poll
            if (!optionsField.getName().equals("Options:") && !responsesField.getName().equals("Responses:")) {
                return Mono.empty();
            }


            ArrayList<String> options = new ArrayList<>();
            //Ensure extra fields are added
            for (int i = 0; i < pollEmbed.getFields().size(); i++) {
                Embed.Field currentField = pollEmbed.getFields().get(i);
                if (currentField.getName().equals("Options:")) {
                    String[] newOptionsArr = currentField.getValue().split("\n");
                    options.addAll(Arrays.asList(newOptionsArr));
                }
            }
            //Add a new option if it is present
            if (newOption != null) {
                options.add(newOption);
                //Remove the first empty option if it exists
                if (options.get(0).equals("\u200e")) {
                    options.remove(0);
                }
            }


            ArrayList<String> emojiBars = calculateEmotes(getResponses(options.size(), message.getReactions()));

            //Ensure that the author is present
            String authorName;
            String iconURL;
            if (pollEmbed.getAuthor().isPresent()) {
                authorName = pollEmbed.getAuthor().get().getName().orElse("A user");
                iconURL = pollEmbed.getAuthor().get().getIconUrl().orElse("");
            } else {
                return Mono.empty();
            }

            String description = pollEmbed.getDescription().orElse("");
            String title = pollEmbed.getTitle().orElse("No question was provided :(");

            //Get the optional thumbnail image
            String thumbnailUrl;
            if (pollEmbed.getThumbnail().isPresent()) {
                thumbnailUrl = pollEmbed.getThumbnail().get().getUrl();
            } else {
                thumbnailUrl = "";
            }

            EmbedCreateSpec newPollEmbed = embeds.createPollEmbed(authorName, description, iconURL, title, emojiBars, thumbnailUrl, options);

            //TODO: TIDY BELOW THIS POINT

            List<EmbedCreateSpec> embed = List.of(newPollEmbed);

            MessageEditSpec editSpec = MessageEditSpec.builder()
                    .embeds(embed)
                    .build();

            HashMap<Snowflake, Boolean> reactionMap = new HashMap<>();
            Mono<Object> completeUserMono = Mono.empty();
            for (Reaction currentReaction : message.getReactions()) {
                Mono<Object> userMono = message.getReactors(currentReaction.getEmoji()).collectList().map(userList -> {
                    for (User user : userList) {
                        reactionMap.put(user.getId(), Boolean.TRUE);
                    }
                    return Mono.empty();
                });
                completeUserMono = completeUserMono.then(userMono);
            }

            if (newOption == null) {
                return message.edit(editSpec).then(completeUserMono);
            } else {
                Button button = null;
                String customId = "poll:delete:";
                String label = "X";

                if (options.size() >= MAX_NUMBER_OF_OPTIONS) {
                    for(LayoutComponent component : message.getComponents()) {
                        for (MessageComponent messageComponent : component.getChildren()) {
                            if (messageComponent.getData().customId().get().startsWith("poll:delete:")) {
                                customId = messageComponent.getData().customId().get();
                                label = messageComponent.getData().label().get();
                            }
                        }
                    }
                    button = Button.danger(customId, label);
                    return message.edit(editSpec.withComponents(ActionRow.of(button))).then(message.addReaction(ReactionEmoji.unicode(POLL_REACTIONS[options.size()-1]))).then(completeUserMono);

                }
                return message.edit(editSpec).then(message.addReaction(ReactionEmoji.unicode(POLL_REACTIONS[options.size()-1]))).then(completeUserMono);
            }
        });
    }

    public Mono<Boolean> deletePoll(Message message, Snowflake buttonUserId, String authorId) {
        if (buttonUserId.asString().equals(authorId)) {
            return message.delete().thenReturn(true);
        } else {
            return Mono.just(false);
        }
    }

    /**
     * Get the list of valid reactions for a poll
     * @return the list of valid reactions for a poll
     */
    public static List<String> getReactsList() {
        return reactsList;
    }

    /**
     * Get the max number of options a poll can have
     * @return the max number
     */
    public static int getMaxNumberOfOptions() {
        return Poll.MAX_NUMBER_OF_OPTIONS;
    }
}
