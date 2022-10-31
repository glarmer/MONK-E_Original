package com.lordnoisy.hoobabot.utility;

import com.lordnoisy.hoobabot.Poll;
import com.lordnoisy.hoobabot.WebImageSearch;
import com.lordnoisy.hoobabot.utility.Utilities;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EmbedBuilder {
    private static final Color pinkWeekColor = Color.of(0xdd49a7);
    private static final Color standardColor = Color.of(0x896E4A);
    private static final String footerText = "Hoobabot Remastered ULTRA edition gold plus";
    private static final String pinkRecyclingBinURL = "https://hoobastinki.es/discord/images/pinkRecyclingBin.png";
    private static final String pinkRecycleLogo = "https://hoobastinki.es/discord/images/pinkRecycleLogo.png";
    private static final String swanseaCouncilURL = "https://www.swansea.gov.uk/kerbsidecollections";
    private static final String footerIconURL = "https://hoobastinki.es/discord/images/footerIcons/";
    private static final int MAX_NUMBER_OF_OPTIONS = Poll.getMaxNumberOfOptions();

    private static Color greenWeekColor = Color.of(0x79E357);
    private static WebImageSearch webImageSearch;
    public EmbedBuilder(WebImageSearch webImageSearch) {
        this.webImageSearch = webImageSearch;
    }

    public EmbedCreateSpec pinkWeekEmbedMaker(boolean isReminder){
        return EmbedCreateSpec.builder()
                .color(pinkWeekColor)
                .author(getTitleString(isReminder, "pink"), swanseaCouncilURL, pinkRecycleLogo)
                .description("It's pink week, that includes the pink, black and food bins.")
                .thumbnail(pinkRecyclingBinURL)
                .timestamp(Instant.now())
                .footer(footerText, (footerIconURL + String.valueOf(Utilities.getRandomNumber(0,156)) + ".png"))
                .build();
    }

    public EmbedCreateSpec greenWeekEmbedMaker(boolean isReminder){
        final String greenRecycleLogo = "https://hoobastinki.es/discord/images/greenRecycleLogo.png";
        final String greenRecyclingBinURL = "https://hoobastinki.es/discord/images/greenRecyclingBin.png";
        return EmbedCreateSpec.builder()
                .color(greenWeekColor)
                .author(getTitleString(isReminder, "green"), swanseaCouncilURL, greenRecycleLogo)
                .description("It's green week, that includes green bins and the food bins.")
                .thumbnail(greenRecyclingBinURL)
                .timestamp(Instant.now())
                .footer(footerText, (footerIconURL + String.valueOf(Utilities.getRandomNumber(0,156)) + ".png"))
                .build();
    }

    private String getTitleString(boolean isReminder, String colour){
        if (colour.equals("green")){
            if (isReminder){
                return "Have you done the bins? (Green Week)";
            } else {
                return "Green Week";
            }
        } else {
            if (isReminder){
                return "Have you done the bins? (Pink Week)";
            } else {
                return "Pink Week";
            }
        }
    }

    public EmbedCreateSpec createMonkeyEmbed(String monkeyName, String monkeyURL, String monkeyDescription){
        return EmbedCreateSpec.builder()
                .color(standardColor)
                .author(monkeyName, monkeyURL, monkeyURL)
                .description(monkeyDescription)
                .image(monkeyURL)
                .timestamp(Instant.now())
                .footer(footerText, (footerIconURL + String.valueOf(Utilities.getRandomNumber(0,156)) + ".png"))
                .build();
    }

    public EmbedCreateSpec createModuleEmbed(ArrayList<String> modulesArrayList, String course, String username, String URL){
        String modules = "";
        for (int i = 0; i < modulesArrayList.size(); i++) {
            modules = modules + modulesArrayList.get(i) + "\n";
        }

        String title = username;
        if (course.equals("Computer Science") || course.equals("Software Engineering")) {
            title = username + " studies " + course +"!";
        }

        return EmbedCreateSpec.builder()
                .color(standardColor)
                .author(title, URL, URL)
                .description(modules)
                .timestamp(Instant.now())
                .footer(footerText, (footerIconURL + String.valueOf(Utilities.getRandomNumber(0,156)) + ".png"))
                .build();
    }

    public EmbedCreateSpec constructQuoteEmbed(String quote, String author) throws IOException {

        String imageURL = webImageSearch.getImageURL(author);
        if (imageURL == null){
            return this.constructFastQuoteEmbed(quote,author);
        } else if (imageURL.length() > 2048) {
            return this.constructFastQuoteEmbed(quote,author);
        }

        return EmbedCreateSpec.builder()
                .color(standardColor)
                .title(author)
                .description(quote)
                .thumbnail(imageURL)
                .timestamp(Instant.now())
                .footer(footerText, (footerIconURL + String.valueOf(Utilities.getRandomNumber(0,156)) + ".png"))
                .build();
    }

    public EmbedCreateSpec constructFastQuoteEmbed(String quote, String author) {
        return EmbedCreateSpec.builder()
                .color(standardColor)
                .title(author)
                .description(quote)
                .timestamp(Instant.now())
                .footer(footerText, (footerIconURL + String.valueOf(Utilities.getRandomNumber(0,156)) + ".png"))
                .build();
    }

    public EmbedCreateSpec createPollEmbed(String title, String description, String profileImgURL, String question, ArrayList<String> emojis, String imageUrl, ArrayList<String> optionsArray) {
        String responsesStringFieldContent = "";
        String responsesEmojiFieldContent = "";
        List<String> reacts = Poll.getReactsList();
        boolean validOptions = true;

        for (int i = 0; i < optionsArray.size() && i < 5; i++) {
            //Stops people putting big whitespace in front of poll
            String currentOption = optionsArray.get(i);
            System.out.println("EMBED BUILDING CURRENT OPTION IS " + currentOption);
            while (i > 0 && currentOption.startsWith(" ")){
                optionsArray.set(i, optionsArray.get(i).replaceFirst(" ", ""));
            }
            responsesStringFieldContent = responsesStringFieldContent.concat(optionsArray.get(i)).concat("\n");
        }

        for (int i = 0; i < emojis.size() && i < 5; i++) {
            responsesEmojiFieldContent = responsesEmojiFieldContent + reacts.get(i) + emojis.get(i) + "\n";
        }

        if (responsesStringFieldContent.equals("")) {
            responsesStringFieldContent = "\u200E";
            responsesEmojiFieldContent = "\u200E";
        }

        EmbedCreateSpec.Builder pollEmbedUnfinished = EmbedCreateSpec.builder()
                .color(standardColor)
                .author(title,  profileImgURL, profileImgURL)
                .title(question)
                .addField("Options:", responsesStringFieldContent, true)
                .addField("Responses:", responsesEmojiFieldContent, true)
                .timestamp(Instant.now())
                .footer(footerText, (footerIconURL + String.valueOf(Utilities.getRandomNumber(0,156)) + ".png"));

        if (imageUrl != null){
            pollEmbedUnfinished.thumbnail(imageUrl);
        }

        if (optionsArray.size() >= 5) {
            if (optionsArray.size() > this.MAX_NUMBER_OF_OPTIONS) {
                validOptions = false;
            }
            float numberOfRemainingOptions = optionsArray.size() - 5;
            double fieldsRequired = Math.ceil(numberOfRemainingOptions / (float) 5);

            int stringOffset = 5;
            int emojiOffset = 5;
            for (int loop = 0; loop < fieldsRequired; loop++) {
                String stringFieldContent = "";
                String emojiFieldContent = "";

                for (int i = stringOffset; i < optionsArray.size() && i < 5 + stringOffset && i < MAX_NUMBER_OF_OPTIONS; i++) {
                    //Stops people putting big whitespace in front of poll
                    while (i > 0 && optionsArray.get(i).startsWith(" ")) {
                        optionsArray.set(i, optionsArray.get(i).replaceFirst(" ", ""));
                    }
                    System.out.println("NEW EMBED OPTION LOOP : " + optionsArray.get(i));
                    stringFieldContent = stringFieldContent + optionsArray.get(i) + "\n";
                }
                for (int i = emojiOffset; i < emojis.size() && i < 5 + emojiOffset && i < MAX_NUMBER_OF_OPTIONS; i++) {
                    emojiFieldContent = emojiFieldContent + reacts.get(i) + emojis.get(i) + "\n";
                }

                stringOffset += 5;
                emojiOffset += 5;
                pollEmbedUnfinished.addField("\u200E", "\u200E", true);
                pollEmbedUnfinished.addField("Options:", stringFieldContent, true);
                pollEmbedUnfinished.addField("Responses:", emojiFieldContent, true);
            }
            pollEmbedUnfinished.addField("\u200E", "\u200E", true);
        }
        if (description == null) {
            description = "";
        }
        if (validOptions) {
            pollEmbedUnfinished.description(description);
        } else {
            pollEmbedUnfinished.description(description + "Note: this poll's options were reduced as the creator inputted more than the max amount of options (20).");
        }

        EmbedCreateSpec pollEmbed = pollEmbedUnfinished.build();

        return pollEmbed;
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
                .color(standardColor)
                .author(author + " sent an image of \"" + queryText + "\":", authorImgURL, authorImgURL)
                .image(imageURL)
                .timestamp(Instant.now())
                .footer(footerText, (footerIconURL + String.valueOf(Utilities.getRandomNumber(0,156)) + ".png"))
                .build();
        return imageEmbed;
    }

    public EmbedCreateSpec constructInsufficientPermissionsEmbed(){
        return EmbedCreateSpec.builder()
                .color(standardColor)
                .title("Insufficient Permissions: You need to be an Administrator for this.")
                .timestamp(Instant.now())
                .footer(footerText, (footerIconURL + String.valueOf(Utilities.getRandomNumber(0,156)) + ".png"))
                .build();
    }

    public EmbedCreateSpec constructReminderChannelSetEmbed(){
        return EmbedCreateSpec.builder()
                .color(standardColor)
                .title("You have successfully set this channel as the bin reminders channel")
                .timestamp(Instant.now())
                .footer(footerText, (footerIconURL + String.valueOf(Utilities.getRandomNumber(0,156)) + ".png"))
                .build();
    }

    public EmbedCreateSpec constructWebSearchEmbed(String website, String author, String authorImgURL, String queryText, String image){
        queryText = queryText.replace("+", " ");

        EmbedCreateSpec imageEmbed = EmbedCreateSpec.builder()
                .color(standardColor)
                .author(author + "'s feeling lucky!", authorImgURL, authorImgURL)
                .description("\n __**Result:**__ \n" +website + "\n\n __**Search Term:**__ \n||" + queryText + "||")
                .image(image)
                .timestamp(Instant.now())
                .footer(footerText, (footerIconURL + String.valueOf(Utilities.getRandomNumber(0,156)) + ".png"))
                .build();
        return imageEmbed;
    }

    public EmbedCreateSpec getOCREmbed(MessageCreateEvent event, String ocrString){
        String author = "";
        String authorImgURL = "";
        String titleURL = "";


        try {
            authorImgURL = event.getMessage().getAuthor().get().getAvatarUrl();
            author = event.getMember().get().getNickname().get();
        } catch (Exception e) {
            e.printStackTrace();
            author = event.getMessage().getAuthor().get().getUsername();
        }

        try {
            String message = event.getMessage().getContent();
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
                .color(standardColor)
                .author(author + ", here is the OCR of your image!", titleURL, authorImgURL)
                .description("```"+ocrString+"```")
                .timestamp(Instant.now())
                .footer(footerText, (footerIconURL + String.valueOf(Utilities.getRandomNumber(0,156)) + ".png"))
                .build();

        return ocrEmbed;
    }

    public EmbedCreateSpec constructHelpEmbed(){
        EmbedCreateSpec helpEmbed = EmbedCreateSpec.builder()
                .color(standardColor)
                .title("Unga Bunga how make bot do?")
                .addField("General bot usage:","Hoobabot is at the forefront of modern technology, and so as such commands can be written as `;<command>` - wow!", false)
                .addField("Monkey:","OO AA OO `;monkey` returns a random monkey from the database, you can follow this up with `id: <id>` to get a specific monkey, then `name: <name>` to name a monkey and finally `description: <description>` to describe a monkey. For example `;monkey id: 102 name: Shh Monkey description: A cheeky little fella tryna keep you quiet.`", false)
                .addField("Polls:","`;poll \"<question>\"` will start a 'Yes/No' style poll. Alternatively up to five custom responses can be chosen using the format: `;poll \"<question>\" \"<response>\" ... \"<response5>\"`. Questions have a character limit of 255 characters and responses have a limit of 40. Uploading an image with your poll command will include the image in the poll. You may delete your poll by reacting with the cross.", false)
                .addField("Bins:","The `;bins` command can be used to tell you what bin week it is in Swansea, `;pink` and `;green` can be used to test each embed.", false)
                .addField("Quote:","`;quote` can be used to get a *questionably* inspirational quote.", false)
                .addField("Image search:","`;image <search query>` will return a \"I'm feeling lucky\" style image search. First 100 a day are from Google, the rest are from Bing. To get a result without an embed `;bing/google <search query>`. Add `-g` to a search for a gif result.", false)
                .addField("YouTube voice functionality:","Join a voice channel, type `;join` , then `;play <link>` or then `;play <search>`. The bot will queue songs. Volume can be controlled with `;volume <number>` (the default is 50) and songs can be skipped with `;skip`.", false)
                .addField("Time:","`;time` is a test command that simply returns the current datetime the bot is working with, it updates roughly every 30 seconds.", false)
                .timestamp(Instant.now())
                .footer(footerText, (footerIconURL + String.valueOf(Utilities.getRandomNumber(0,156)) + ".png"))
                .build();
        return helpEmbed;
    }

    public EmbedCreateSpec constructPollHelpEmbed(){
        EmbedCreateSpec helpEmbed = EmbedCreateSpec.builder()
                .color(standardColor)
                .title("Monkey see, monkey poll?")
                .addField("Polls:","`;poll \"<question>\"` will start a 'Yes/No' style poll. Alternatively up to five custom responses can be chosen using the format: `;poll \"<question>\" \"<response>\" ... \"<response5>\"`. Questions have a character limit of 255 characters and responses have a limit of 40 each. Uploading an image with your poll command will include the image in the poll. You may delete your poll by reacting with the cross.", false)
                .timestamp(Instant.now())
                .footer(footerText, (footerIconURL + String.valueOf(Utilities.getRandomNumber(0,156)) + ".png"))
                .build();
        return helpEmbed;
    }

    public EmbedCreateSpec constructImageHelpEmbed(){
        EmbedCreateSpec helpEmbed = EmbedCreateSpec.builder()
                .color(standardColor)
                .title("Monkey want image?")
                .addField("Image search:","`;image <search query>` will return a \"I'm feeling lucky\" style image search. First 100 a day are from Google, the rest are from Bing. To get a result without an embed `;bing/google <search query>`. Add `-g` to a search for a gif result.", false)
                .timestamp(Instant.now())
                .footer(footerText, (footerIconURL + String.valueOf(Utilities.getRandomNumber(0,156)) + ".png"))
                .build();
        return helpEmbed;
    }

    public EmbedCreateSpec constructNoImageFoundEmbed() {
        EmbedCreateSpec noImageEmbed = EmbedCreateSpec.builder()
                .color(standardColor)
                .title("No Image Found")
                .description("Sorry, try refining your search into something clearer.")
                .timestamp(Instant.now())
                .footer(footerText, (footerIconURL + String.valueOf(Utilities.getRandomNumber(0,156)) + ".png"))
                .build();
        return noImageEmbed;
    }

    public EmbedCreateSpec constructSearchingEmbed() {
        EmbedCreateSpec searchEmbed = EmbedCreateSpec.builder()
                .color(standardColor)
                .title("Searching...")
                .description("This may take a moment.")
                .timestamp(Instant.now())
                .footer(footerText, (footerIconURL + String.valueOf(Utilities.getRandomNumber(0,156)) + ".png"))
                .build();
        return searchEmbed;
    }

    public EmbedCreateSpec constructErrorEmbed() {
        EmbedCreateSpec errorEmbed = EmbedCreateSpec.builder()
                .color(standardColor)
                .title("Oopsie Poopsie, there's been an error :(")
                .description("Please try again or contact the bot admin if the error persists.")
                .timestamp(Instant.now())
                .footer(footerText, (footerIconURL + String.valueOf(Utilities.getRandomNumber(0,156)) + ".png"))
                .build();
        return errorEmbed;
    }

    public static Color getPinkWeekColor() {
        return pinkWeekColor;
    }

    public static Color getStandardColor() {
        return standardColor;
    }

    public static String getFooterText() {
        return footerText;
    }

    public static String getPinkRecyclingBinURL() {
        return pinkRecyclingBinURL;
    }

    public static String getPinkRecycleLogo() {
        return pinkRecycleLogo;
    }

    public static String getSwanseaCouncilURL() {
        return swanseaCouncilURL;
    }

    public static String getFooterIconURL() {
        return footerIconURL;
    }

    public static int getMAX_NUMBER_OF_OPTIONS() {
        return MAX_NUMBER_OF_OPTIONS;
    }

    public static Color getGreenWeekColor() {
        return greenWeekColor;
    }

    public static WebImageSearch getWebImageSearch() {
        return webImageSearch;
    }
}
