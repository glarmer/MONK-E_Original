package com.lordnoisy.hoobabot;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.reaction.Reaction;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateFields;
import discord4j.core.spec.MessageEditSpec;
import discord4j.discordjson.json.EmbedAuthorData;
import discord4j.rest.util.Color;
import org.apache.commons.io.FileUtils;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Poll {
    private static final String one = "<:1_:902952221761753148>";
    private static final String two = "<:2_:902952173674057738>";
    private static final String three = "<:3_:902952173644677170>";
    private static final String four = "<:4_:902952173560815676>";
    private static final String five = "<:5_:902952173531431012>";
    private static final String six = "<:6_:902952174986862622>";
    private static final String seven = "<:7_:902952173489508383>";
    private static final String eight = "<:8_:902952173741166632>";
    private static final String nine = "<:9_:902952173623705640>";
    private static final String[] reacts = new String[] {"\u0031\u20E3", "\u0032\u20E3",
            "\u0033\u20E3", "\u0034\u20E3", "\u0035\u20E3"};
    private static final List<String> reactsList = List.of(reacts);
    private static final String crossReact = "\u274c";
    private EmbedBuilder embeds;

    public Poll(EmbedBuilder embeds){
        this.embeds = embeds;
    }

    //TODO: SORT THIS SHIT OUT, WHAT WERE YOU THINKING

    public String[] calculateEmotes(int[] responses) {
        //Most responses instead of total, since this is relative
        double mostResponses = 0;
        String[] responseEmojis = new String[responses.length];

        for (int i = 0; i < responses.length; i++) {
            if (mostResponses < responses[i]) {
                mostResponses = responses[i];
            }
        }
        if (mostResponses < 1){
            mostResponses++;
        }

        for (int i = 0; i < responses.length; i++) {
            //Change mostResponses to totalResponses to get an absolute percentage
            double percentage = ((responses[i] / mostResponses) * 100);

            String emojis = "|";
            int iterations = 0;
            while (percentage >= 0 && iterations < 8) {
                if (percentage >= 12.5) {
                    emojis = emojis + nine;
                    percentage = percentage - 12.5;
                } else if (percentage >= 10.9375) {
                    emojis = emojis + eight;
                    percentage = percentage - 10.9375;
                } else if (percentage >= 9.375) {
                    emojis = emojis + seven;
                    percentage = percentage - 9.375;
                } else if (percentage >= 7.8125) {
                    emojis = emojis + six;
                    percentage = percentage - 7.8125;
                } else if (percentage >= 6.25) {
                    emojis = emojis + five;
                    percentage = percentage - 6.25;
                } else if (percentage >= 4.6875) {
                    emojis = emojis + four;
                    percentage = percentage - 4.6875;
                } else if (percentage >= 3.125) {
                    emojis = emojis + three;
                    percentage = percentage - 3.125;
                } else if (percentage >= 1.5625) {
                    emojis = emojis + two;
                    percentage = percentage - 1.5625;
                } else {
                    iterations++;

                    int needed = 8 - iterations;

                    for (int j = 0; j <= needed; j++) {
                        emojis = emojis + one;
                    }
                    break;
                }
                iterations++;
            }
            responseEmojis[i] = emojis;
        }
        return responseEmojis;
    }

    public Mono<String> uploadImage(String attachedUrl, GatewayDiscordClient gateway) {
        //Re-upload the image elsewhere
        try {
            String generatedString = StringUtilities.getRandomString(10);

            //Get the file extension
            String fileExtension = attachedUrl.split("\\.")[3];

            String fileName = "image_" + generatedString + "." + fileExtension;

            //Download the file
            File image = new File(fileName);
            FileUtils.copyURLToFile(
                    new URL(attachedUrl),
                    image,
                    500,
                    10000);

            //Reupload the file
            InputStream fileInputStream = new FileInputStream(image);
            MessageCreateFields.File messageCreateFields = MessageCreateFields.File.of(fileName, fileInputStream);

            Mono<String> uploadMono = gateway.getChannelById(Snowflake.of(945092365989867560L))
                    .ofType(GuildMessageChannel.class)
                    .flatMap(uploadChannel -> uploadChannel.createMessage().withFiles(messageCreateFields))
                    .flatMap(message -> Mono.just(message.getAttachments().get(0).getUrl()));

            fileInputStream.close();
            image.delete();

            return uploadMono;
        } catch (Exception e) {
            e.printStackTrace();
            return Mono.empty();
        }
    }

    public ArrayList<String> getOptionsArray(Message message) {
        String[] options;
        String optionsStr = "";

        try {
            //Pattern match for questions + responses
            ArrayList<String> allMatches = getMatchesFromString(message.getContent());
            //Set up the options array
            options = new String[allMatches.size() - 1];
            if (allMatches.size() - 1 > 5) {
                options = new String[5];
            }

            //Get the options and ensure they're not too large, correcting if they are.
            for (int i = 0; i < options.length; i++) {
                String currentOption = allMatches.get(i + 1);
                if (currentOption.length() > 40) {
                    currentOption = currentOption.substring(0, 36) + "...";
                }
                options[i] = currentOption;
                optionsStr = optionsStr + "\"" + currentOption + "\"";
            }

            if (options.length < 1){
                options = new String[] {"Yes", "No", "Maybe"};
                optionsStr = "\"Yes\" \"No\" \"Maybe\"";
            }
        } catch (Exception e) {
            e.printStackTrace();
            optionsStr = "\"Yes\" \"No\" \"Maybe\"";
        }
        return getMatchesFromString(optionsStr);
    }

    /**
     * Add reaction emotes to a poll message
     * @return the emotes mono
     */
    public Mono<Void> addPollReacts(Message message, Message userMessage) {
        ArrayList<String> optionsArray = getOptionsArray(userMessage);
        Mono<Void> addEmotes = Mono.empty();
        try {
            for (int i = 0; i < optionsArray.size(); i++) {
                addEmotes = addEmotes.and(message.addReaction(ReactionEmoji.unicode(reacts[i])));
            }
            //add X reaction
            addEmotes = addEmotes.and(message.addReaction(ReactionEmoji.unicode(crossReact)));
        } catch (Exception e) {
            e.printStackTrace();
            addEmotes = Mono.empty();
        }
        return addEmotes;
    }

    public int[] getResponses(int numberOfOptions, List<Reaction> reactions) {
        int[] responses = new int[numberOfOptions];
        for (int i = 0; i < reactions.size(); i++) {
            Reaction currentReaction = reactions.get(i);
            //Find if the reaction is unicode
            ReactionEmoji reactionEmoji = currentReaction.getEmoji();
            if(!DiscordUtilities.isPollEmote(reactionEmoji)) {
                break;
            }
            try {
                String currentName = currentReaction.getEmoji().asUnicodeEmoji().get().getRaw();
                switch (currentName) {
                    case "\u0031\u20E3":
                        responses[0] = currentReaction.getCount() - 1;
                        break;
                    case "\u0032\u20E3":
                        responses[1] = currentReaction.getCount() - 1;
                        break;
                    case "\u0033\u20E3":
                        if (responses.length > 2) {
                            responses[2] = currentReaction.getCount() - 1;
                        }
                        break;
                    case "\u0034\u20E3":
                        if (responses.length > 3) {
                            responses[3] = currentReaction.getCount() - 1;
                        }
                        break;
                    case "\u0035\u20E3":
                        if (responses.length > 4) {
                            responses[4] = currentReaction.getCount() - 1;
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return responses;
    }

    /**
     * Create a poll
     * @param event the event that has triggered poll creation code
     * @return a mono to create a poll
     */
    public Mono<Void> createPoll(MessageCreateEvent event) {
        //TODO: Upload the file alongside the message and then tell the embed to use that for it's thumbnail
        Member member = event.getMember().orElse(null);
        if (member != null) {
            ArrayList<Object> pollData = sharedPoll(event);
            String username = (String) pollData.get(0);
            String profileImgURL = (String) pollData.get(1);
            String question = (String) pollData.get(2);
            String options = (String) pollData.get(3);
            String attachedUrl = (String) pollData.get(4);

            ArrayList<String> optionsArray = getMatchesFromString(options);

            //Prepare empty response emojis
            int[] responses = new int[optionsArray.size()];
            for (int i = 0; i < optionsArray.size(); i++) {
                responses[i] = 0;
            }
            String[] emojis = calculateEmotes(responses);

            EmbedCreateSpec pollEmbed = embeds.createPollEmbed(username, profileImgURL, question, options, emojis, attachedUrl, getMatchesFromString(options));
            return event.getMessage().getChannel()
                    .flatMap(messageChannel -> messageChannel.createMessage(pollEmbed)
                            .flatMap(message -> addPollReacts(message, event.getMessage()))
                    ).then();
        } else {
            return event.getMessage().getChannel()
                    .flatMap(messageChannel -> messageChannel.createMessage(embeds.constructPollHelpEmbed())
                    ).then();
        }
    }

    public Mono<Object> updatePoll(Mono<Message> pollMessage, Snowflake userSnowflake, ReactionEmoji reactedEmoji) {
        //TODO: COPY THUMBNAIL
        return pollMessage.flatMap(message -> {
            //Ensure the message is a bot message
            if (!DiscordUtilities.isBotMessage(message.getClient(), message)) {
                return Mono.empty();
            }

            Embed pollEmbed = null;
            String optionsFieldTitle = "";
            try {
                pollEmbed = message.getEmbeds().get(0);
                optionsFieldTitle = pollEmbed.getFields().get(0).getName();
                if (!optionsFieldTitle.startsWith("Options:")) {
                    return Mono.empty();
                }
            } catch (NoSuchElementException noSuchElementException) {
                return Mono.empty();
            }

            Embed.Thumbnail thumbnail = pollEmbed.getThumbnail().orElse(null);

            String thumbnailUrl = "";
            if (thumbnail != null) {
                thumbnailUrl = thumbnail.getUrl();
            }

            Embed.Author author = pollEmbed.getAuthor().get();
            EmbedAuthorData authorData = author.getData();
            String authorURL = authorData.url().get();
            String name = authorData.name().get();
            String iconURL = authorData.iconUrl().get();
            String authorID = iconURL.split("/")[4];

            if (DiscordUtilities.isBeingDeleted(reactedEmoji, authorID, userSnowflake.asString())) {
                return message.delete();
            }

            Color color = pollEmbed.getColor().get();
            String title = pollEmbed.getTitle().get();
            Instant timestamp = Instant.now();
            String options = pollEmbed.getFields().get(0).getValue();
            int numberOfOptions = options.split("\n").length;

            int[] responses = getResponses(numberOfOptions, message.getReactions());
            String[] emojiBars = calculateEmotes(responses);

            String responsesEmojiFieldContent = "";
            for (int i = 0; i < emojiBars.length; i++) {
                responsesEmojiFieldContent = responsesEmojiFieldContent + emojiBars[i] + "\n";
            }

            EmbedCreateSpec newPollEmbed = EmbedCreateSpec.builder()
                    .color(color)
                    .author(name, authorURL, iconURL)
                    .title(title)
                    .addField(optionsFieldTitle, pollEmbed.getFields().get(0).getValue(), true)
                    .addField(pollEmbed.getFields().get(1).getName(), responsesEmojiFieldContent, true)
                    .thumbnail(thumbnailUrl)
                    .timestamp(timestamp)
                    .footer("Hoobabot Remastered ULTRA edition gold plus", ("https://hoobastinki.es/discord/images/footerIcons/" + String.valueOf(RandomNumberGen.getRandomNumber(0,156)) + ".png"))
                    .build();

            List<EmbedCreateSpec> embed = List.of(newPollEmbed);

            MessageEditSpec editSpec = MessageEditSpec.builder()
                    .embeds(embed)
                    .build();

            return message.edit(editSpec);
        });
    }

    public ArrayList<String> getMatchesFromString(String string){
        //Pattern match for questions + responses
        String strPattern = "\"[^\"]*\"";
        Pattern pattern = Pattern.compile(strPattern);
        Matcher matcher = pattern.matcher(string);
        ArrayList<String> allMatches = new ArrayList<>();
        while (matcher.find()) {
            allMatches.add(matcher.group().replaceAll("\"", ""));
        }
        return allMatches;
    }

    /**
     * This is the main poll code, it is shared between finaliseStartPoll and startPoll
     * @param event the message create event
     * @return an arraylist of the necessary data for the poll
     */
    public ArrayList<Object> sharedPoll(MessageCreateEvent event){
        Message message = event.getMessage();
        ArrayList<Object> pollData = new ArrayList<>();
        String attachedUrl = null;
        String[] options;
        String optionsStr = "";
        String question = null;

        //Get the username and profile image
        String username;
        String profileImgURL;
        String userID;
        try {
            Member author = event.getMember().get();
            username = author.getDisplayName();
            profileImgURL = author.getAvatarUrl();
            userID = author.getId().asString();
        } catch (Exception e) {
            User author = event.getMessage().getAuthor().get();
            username = author.getUsername();
            profileImgURL = author.getAvatarUrl();
            userID = author.getId().asString();
        }

        try {
            //Pattern match for questions + responses
            ArrayList<String> allMatches = getMatchesFromString(message.getContent());

            //Get the first result, which is the question and ensure it is not too large, correct if it is
            question = allMatches.get(0);
            if (question.length() > 255) {
                question = question.substring(0, 251) + "...";
            }

            //Set up the options array
            options = new String[allMatches.size() - 1];
            if (allMatches.size() - 1 > 5) {
                options = new String[5];
            }

            //Get the options and ensure they're not too large, correcting if they are.
            for (int i = 0; i < options.length; i++) {
                String currentOption = allMatches.get(i + 1);
                if (currentOption.length() > 40) {
                    currentOption = currentOption.substring(0, 36) + "...";
                }
                options[i] = currentOption;
                optionsStr = optionsStr + "\"" + currentOption + "\"";
            }

            if (options.length < 1){
                options = new String[] {"Yes", "No", "Maybe"};
                optionsStr = "\"Yes\" \"No\" \"Maybe\"";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Check if there is an attached image and save the link if there is.
        try {
            List<Attachment> attachments = message.getAttachments();
            for (int i = 0; i < attachments.size(); i++) {
                if(attachments.get(i).getContentType().get().contains("image")) {
                    attachedUrl = attachments.get(i).getUrl();
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("No image");
        }

        //Add all data to the arraylist

        pollData.add(username);
        pollData.add(profileImgURL);
        pollData.add(question);
        pollData.add(optionsStr);
        pollData.add(attachedUrl);
        pollData.add(userID);

        return pollData;
    }
}
