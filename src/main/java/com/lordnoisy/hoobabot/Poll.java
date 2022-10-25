package com.lordnoisy.hoobabot;

import com.lordnoisy.hoobabot.utility.DiscordUtilities;
import com.lordnoisy.hoobabot.utility.EmbedBuilder;
import com.lordnoisy.hoobabot.utility.Utilities;
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
import org.apache.commons.io.FileUtils;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final String[] POLL_REACTIONS = new String[] { POLL_OPTION_ONE, POLL_OPTION_TWO,
            POLL_OPTION_THREE, POLL_OPTION_FOUR, POLL_OPTION_FIVE, POLL_OPTION_SIX, POLL_OPTION_SEVEN, POLL_OPTION_EIGHT, POLL_OPTION_NINE,
            POLL_OPTION_TEN, POLL_OPTION_ELEVEN, POLL_OPTION_TWELVE, POLL_OPTION_THIRTEEN, POLL_OPTION_FOURTEEN, POLL_OPTION_FIFTEEN,
            POLL_OPTION_SIXTEEN, POLL_OPTION_SEVENTEEN, POLL_OPTION_EIGHTEEN, POLL_OPTION_NINETEEN};
    private static final List<String> reactsList = List.of(POLL_REACTIONS);
    private static final String DELETE_CROSS_REACT = "\u274c";
    private final EmbedBuilder embeds;

    public Poll(EmbedBuilder embeds){
        this.embeds = embeds;
    }

    /**
     * Create the bars of emojis that represent poll votes
     * @param responses the number of responses to the poll for each option
     * @return an array of bars
     */
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
            responseEmojis[i] = emojis;
        }
        return responseEmojis;
    }

    //TODO: Re-add this functionality, possibly using some of this method as a guide
    public Mono<String> uploadImage(String attachedUrl, GatewayDiscordClient gateway) {
        //Re-upload the image elsewhere
        try {
            String generatedString = Utilities.getRandomString(10);

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

    /**
     * Gets the question and options from a message string
     * @param string the message
     * @return a list of the question followed by the options
     */
    public ArrayList<String> getQuestionAndAnswers(String string){
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
    //TODO: Figure out what is going on here and make it make sense, this code is grim
    public ArrayList<String> getOptionsArray(Message message) {
        String[] options;
        String optionsStr = "";

        try {
            //Pattern match for questions + responses
            ArrayList<String> allMatches = getQuestionAndAnswers(message.getContent());
            //Set up the options array
            options = new String[allMatches.size() - 1];
            if (allMatches.size() - 1 > 19) {
                options = new String[19];
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
        return getQuestionAndAnswers(optionsStr);
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
                addEmotes = addEmotes.and(message.addReaction(ReactionEmoji.unicode(POLL_REACTIONS[i])));
            }
            //add X reaction
            addEmotes = addEmotes.and(message.addReaction(ReactionEmoji.unicode(DELETE_CROSS_REACT)));
        } catch (Exception e) {
            e.printStackTrace();
            addEmotes = Mono.empty();
        }
        return addEmotes;
    }

    /**
     * Get the number of responses of each option
     * @param numberOfOptions how many options the poll has
     * @param reactions the reactions from the poll message
     * @return an array of the responses in order
     */
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
                //TODO: There has to be a better way of doing this
                String currentName = currentReaction.getEmoji().asUnicodeEmoji().get().getRaw();
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

            ArrayList<String> optionsArray = getQuestionAndAnswers(options);

            //Prepare empty response emojis
            int[] responses = new int[optionsArray.size()];
            for (int i = 0; i < optionsArray.size(); i++) {
                responses[i] = 0;

            }
            String[] emojis = calculateEmotes(responses);

            EmbedCreateSpec pollEmbed = embeds.createPollEmbed(username, profileImgURL, question, emojis, attachedUrl, getQuestionAndAnswers(options));
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

    /**
     * Update the poll to represent vote distribution
     * @param pollMessage the original poll message
     * @param userSnowflake the snowflake of the user
     * @param reactedEmoji the emoji that was reacted onto the poll
     * @return a Mono to update the poll
     */
    public Mono<Object> updatePoll(Mono<Message> pollMessage, Snowflake userSnowflake, ReactionEmoji reactedEmoji) {
        //TODO: More checks to avoid not-needed edits
        //TODO: TIDY -> Maybe split up into smaller methods
        return pollMessage.flatMap(message -> {
            //Ensure the message is a bot message
            if (!DiscordUtilities.isBotMessage(message.getClient(), message)) {
                return Mono.empty();
            }

            Embed pollEmbed;
            String optionsFieldTitle;
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
            String name = authorData.name().get().split(" ")[0];
            String iconURL = authorData.iconUrl().get();
            String authorID = iconURL.split("/")[4];

            if (DiscordUtilities.isBeingDeleted(reactedEmoji, authorID, userSnowflake.asString())) {
                return message.delete();
            }
            String title = pollEmbed.getTitle().get();
            String options = "";

            String pollOptions = "";
            List<Embed.Field> fields = pollEmbed.getFields();
            for (Embed.Field field : fields) {
                if (field.getName().equals("Options:")) {
                    pollOptions = pollOptions + field.getValue() + "\n";
                } else if (field.getName().equals("Responses:")) {
                    options = options + field.getValue() + "\n";
                }
            }

            int numberOfOptions = options.split("\n").length;
            int[] responses = getResponses(numberOfOptions, message.getReactions());
            String[] emojiBars = calculateEmotes(responses);
            String responsesEmojiFieldContent = "";
            for (int i = 0; i < emojiBars.length; i++) {
                responsesEmojiFieldContent = responsesEmojiFieldContent + emojiBars[i] + "\n";
            }

            ArrayList<String> optionsArray = new ArrayList<String>(Arrays.asList(pollOptions.split(":\n")));
            String[] emojis = responsesEmojiFieldContent.split("\n");

            EmbedCreateSpec newPollEmbed = embeds.createPollEmbed(name, iconURL, title, emojis, thumbnailUrl, optionsArray);

            List<EmbedCreateSpec> embed = List.of(newPollEmbed);

            MessageEditSpec editSpec = MessageEditSpec.builder()
                    .embeds(embed)
                    .build();

            return message.edit(editSpec);
        });
    }

    //TODO: This should be changed because it is no longer shared between methods, it's all one method.
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
            ArrayList<String> allMatches = getQuestionAndAnswers(message.getContent());

            //Get the first result, which is the question and ensure it is not too large, correct if it is
            question = allMatches.get(0);
            if (question.length() > 255) {
                question = question.substring(0, 251) + "...";
            }

            //Set up the options array
            options = new String[allMatches.size() - 1];
            if (allMatches.size() - 1 > 19) {
                options = new String[19];
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

    /**
     * Get the list of valid reactions for a poll
     * @return the list of valid reactions for a poll
     */
    public static List<String> getReactsList() {
        return reactsList;
    }
}
