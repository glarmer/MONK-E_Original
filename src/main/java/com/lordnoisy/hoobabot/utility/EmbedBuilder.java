package com.lordnoisy.hoobabot.utility;

import com.lordnoisy.hoobabot.Poll;
import com.lordnoisy.hoobabot.WebImageSearch;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class EmbedBuilder {
    private static final Color STANDARD_COLOR = Color.of(0x896E4A);
    private static final String FOOTER_TEXT = "OOH AHH MONK-E";

    private static final String FOOTER_ICON_URL = "https://glarmer.xyz/monke/img/monke_icons/";
    private static final int MAX_NUMBER_OF_OPTIONS = Poll.getMaxNumberOfOptions();

    private static WebImageSearch webImageSearch;

    public EmbedBuilder(WebImageSearch webImageSearch) {
        this.webImageSearch = webImageSearch;
    }


    public EmbedCreateSpec constructImageEmbed(String imageURL, String author, String authorImgURL, String queryText){
        if (queryText.contains(" -g ")) {
            queryText = queryText.replace(" -g ","");
        } else if (queryText.contains("-g ")) {
            queryText = queryText.replace("-g ","");
        } else if (queryText.contains(" -g")) {
            queryText = queryText.replace(" -g","");
        } else if (queryText.contains("-g")) {
            queryText = queryText.replace("-g","");
        }
        EmbedCreateSpec imageEmbed = EmbedCreateSpec.builder()
                .color(STANDARD_COLOR)
                .author(author + " sent an image of \"" + queryText + "\":", authorImgURL, authorImgURL)
                .image(imageURL)
                .timestamp(Instant.now())
                .footer(FOOTER_TEXT, (FOOTER_ICON_URL + String.valueOf(Utilities.getRandomNumber(0,156)) + ".png"))
                .build();
        return imageEmbed;
    }

    public static EmbedCreateSpec constructInsufficientPermissionsEmbed(){
        return EmbedCreateSpec.builder()
                .color(STANDARD_COLOR)
                .title("Insufficient Permissions: You need to be an Administrator for this.")
                .timestamp(Instant.now())
                .footer(FOOTER_TEXT, (FOOTER_ICON_URL + String.valueOf(Utilities.getRandomNumber(0,156)) + ".png"))
                .build();
    }



    public EmbedCreateSpec constructWebSearchEmbed(String website, String author, String authorImgURL, String queryText, String image){
        queryText = queryText.replace("+", " ");

        EmbedCreateSpec imageEmbed = EmbedCreateSpec.builder()
                .color(STANDARD_COLOR)
                .author(author + "'s feeling lucky!", authorImgURL, authorImgURL)
                .description("\n __**Result:**__ \n" +website + "\n\n __**Search Term:**__ \n||" + queryText + "||")
                .image(image)
                .timestamp(Instant.now())
                .footer(FOOTER_TEXT, (FOOTER_ICON_URL + String.valueOf(Utilities.getRandomNumber(0,156)) + ".png"))
                .build();
        return imageEmbed;
    }

    public EmbedCreateSpec getOCREmbed(ChatInputInteractionEvent event, String ocrString){
        String author = "";
        String authorImgURL = "";
        String titleURL = "";


        try {
            authorImgURL = event.getInteraction().getMember().get().getAvatarUrl();
            author = event.getInteraction().getMember().get().getNickname().get();
        } catch (Exception e) {
            e.printStackTrace();
            author = event.getInteraction().getMember().get().getUsername();
        }

        try {
            String message = "ignore this";
            titleURL = message.split(" ")[1];
        } catch (Exception e){
            e.printStackTrace();
            titleURL = authorImgURL;
        }

        try {
            URL u = new URL(titleURL); // this would check for the protocol
            u.toURI(); // does the extra checking required for validation of URI
        } catch (Exception e) {
            titleURL = authorImgURL;
        }

        EmbedCreateSpec ocrEmbed = EmbedCreateSpec.builder()
                .color(STANDARD_COLOR)
                .author(author + ", here is the OCR of your image!", titleURL, authorImgURL)
                .description("```"+ocrString+"```")
                .timestamp(Instant.now())
                .footer(FOOTER_TEXT, (FOOTER_ICON_URL + String.valueOf(Utilities.getRandomNumber(0,156)) + ".png"))
                .build();

        return ocrEmbed;
    }

    public EmbedCreateSpec constructHelpEmbed(){
        EmbedCreateSpec helpEmbed = EmbedCreateSpec.builder()
                .color(STANDARD_COLOR)
                .title("Botumentation?")
                .addField("General bot usage:","MONK-E is at the forefront of duct tape, spit and glue technology, and so as such it uses slash commands - wow!", false)
                .addField("Monkey:","**Broken Currently.**", false)
                .addField("Polls:","`/poll` can be used to generate a reaction poll. You can have 0 up to 20 unique options. Input no answers to auto generate a Yes/No/Maybe poll! Use the `open_poll` flag to allow others to add options. The `dates_poll` option can be used to generate poll containing dates for planning events! Images can be attached to polls using the `image` option.", false)
                .addField("Date Polls:","`/poll_dates` can be used to generate a dates reaction poll. You can have 0 up to 20 unique dates. Inputting only a question will autogenerate the next 20 days as a poll. Use the `number_of_days` option to select the number of days into the future to go. By using the `interval` option you can choose the number of days between each option, e.g. pick 7 to make a weekly poll. Images can be attached to polls using the `image` option.", false)
                .addField("Bins:","The `/bins` command can be used to tell you what bin week it is in Swansea.", false)
                .addField("Quote:","`/quote` can be used to get a *questionably* inspirational quote.", false)
                .addField("Image search:","`/image` can be used to search google/bing for an image. Optionally you can search for gifs and choose the engine.", false)
                .addField("Video search:","`/video` can be used to search YouTube for a video.", false)
                .addField("YouTube voice functionality:","**Broken Currently** Join a voice channel, type `;join` , then `;play <link>` or then `;play <search>`. The bot will queue songs. Volume can be controlled with `;volume <number>` (the default is 50) and songs can be skipped with `;skip`.", false)
                .addField("Uptime:","`/uptime` will provide you with the current bot uptime, start time and current time.", false)
                .timestamp(Instant.now())
                .footer(FOOTER_TEXT, (FOOTER_ICON_URL + String.valueOf(Utilities.getRandomNumber(0,156)) + ".png"))
                .build();
        return helpEmbed;
    }

    public static EmbedCreateSpec constructErrorEmbed() {
        EmbedCreateSpec errorEmbed = EmbedCreateSpec.builder()
                .color(STANDARD_COLOR)
                .title("Oopsie Poopsie, there's been an error :(")
                .description("Please try again or contact the bot admin if the error persists.")
                .timestamp(Instant.now())
                .footer(FOOTER_TEXT, (FOOTER_ICON_URL + String.valueOf(Utilities.getRandomNumber(0,156)) + ".png"))
                .build();
        return errorEmbed;
    }

    public static Color getStandardColor() {
        return STANDARD_COLOR;
    }

    public static String getFooterText() {
        return FOOTER_TEXT;
    }


    public static String getFooterIconUrl() {
        return FOOTER_ICON_URL;
    }

    public static int getFooterIconNumber() {
        return Utilities.getRandomNumber(0,160);
    }

    public static int getMAX_NUMBER_OF_OPTIONS() {
        return MAX_NUMBER_OF_OPTIONS;
    }


    public static WebImageSearch getWebImageSearch() {
        return webImageSearch;
    }
}
