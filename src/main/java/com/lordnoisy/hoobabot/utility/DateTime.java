package com.lordnoisy.hoobabot.utility;

import com.lordnoisy.hoobabot.Binformation;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.*;
import java.time.DayOfWeek;
import java.util.Timer;
import java.util.TimerTask;

public class DateTime extends TimerTask{
    LocalTime time;
    LocalDate date;
    int dateWeek;
    int dateWeekDay;
    int dateDayHour;
    int reminders = 0;
    boolean isBinsDone = false;
    boolean isDeveloperMode;
    ArrayList<String> channels;
    EmbedBuilder embeds;
    GatewayDiscordClient gateway;

    public DateTime(ArrayList<String> channels, EmbedBuilder embeds, boolean isDeveloperMode, GatewayDiscordClient gatewayDiscordClient) {
        try {
            this.time = LocalTime.now();
            this.date = LocalDate.now();
            TemporalField woy = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear();
            this.dateWeek = date.get(woy);
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            this.dateWeekDay = dayOfWeek.getValue();
            dateDayHour = time.getHour();
            this.channels = channels;
            this.embeds = embeds;
            this.isDeveloperMode = isDeveloperMode;
            this.startTimer();
            this.gateway = gatewayDiscordClient;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startTimer() {
        TimerTask timerTask = this; //reference created for TimerTask class
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(timerTask, 0, 30000); // 1.task 2.delay 3.period
    }

    public void run(){
        this.updateDate();
        this.sendReminderIfDate(embeds);
        //this.sendReminderAsTest(embeds);
    }

    public void updateDate(){
        this.time = LocalTime.now();
        this.date = LocalDate.now();
        TemporalField woy = WeekFields.of(Locale.UK).weekOfWeekBasedYear();
        this.dateWeek = date.get(woy);
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        this.dateWeekDay = dayOfWeek.getValue();
        dateDayHour = time.getHour();
    }

    public void sendReminderIfDate(EmbedBuilder embeds){
        EmbedCreateSpec embedToSend = Binformation.binWeekCalculator(this.dateWeek, this.dateWeekDay, this.dateDayHour, embeds, true);
        if (dateWeekDay == 2 && dateDayHour >= 19 && reminders == 0) {
            for (int i = 0; i < this.channels.size(); i++) {
                Snowflake id = Snowflake.of(channels.get(i));
                gateway.getChannelById(id).ofType(MessageChannel.class).flatMap(channel -> channel.createMessage(embedToSend)).subscribe().dispose();
            }
            reminders += 1;
        } else if (dateWeekDay == 2 && dateDayHour >= 22 && reminders == 1) {
            for (int i = 0; i < this.channels.size(); i++) {
                Snowflake id = Snowflake.of(channels.get(i));
                gateway.getChannelById(id).ofType(MessageChannel.class).flatMap(channel -> channel.createMessage(embedToSend)).subscribe().dispose();
            }
            reminders += 1;
        } else if (dateWeekDay != 2) {
            reminders = 0;
            isBinsDone = false;
        }
    }

    public LocalTime getTime() {
        return time;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getDateWeek() {
        return dateWeek;
    }

    public int getDateWeekDay() {
        return dateWeekDay;
    }

    public int getDateDayHour() {
        return dateDayHour;
    }
}
