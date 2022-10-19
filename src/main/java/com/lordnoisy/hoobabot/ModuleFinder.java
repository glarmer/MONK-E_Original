package com.lordnoisy.hoobabot;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.Id;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ModuleFinder {

    //TODO: REWRITE ALL OF THIS
    /*public EmbedCreateSpec getProfile(MessageCreateEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        try {
            String id = event.getMessage().getContent().split(" ")[1];

            Mono<Member> member = event.getClient().getMemberById(event.getGuildId().get(), Snowflake.of(id));

            AtomicReference<ArrayList> atomicModules = new AtomicReference<>(new ArrayList<String>());
            AtomicReference<String> atomicCourse = new AtomicReference<>(new String());
            AtomicReference<String> atomicUsername = new AtomicReference<>(new String());
            AtomicReference<String> atomicUrl = new AtomicReference<>(new String());

            member.flatMap(user -> {
                List<Id> roles = user.getMemberData().roles();
                atomicUsername.set(user.getDisplayName());
                atomicUrl.set(user.getAvatarUrl());

                for (int i = 0; i < roles.size(); i++) {
                    Mono<Role> currentRole = event.getClient().getRoleById(event.getGuildId().get(), Snowflake.of(roles.get(i)));

                    currentRole.flatMap(role -> {
                        String currentRoleName = role.getName();
                        if (currentRoleName.startsWith("CS3")) {
                            System.out.println("finley banana" + currentRoleName);
                            atomicModules.get().add(currentRoleName);
                        } else if (currentRoleName.startsWith("ComputerScience")) {
                            atomicCourse.set("Computer Science");
                        } else if (currentRoleName.startsWith("SoftwareEng")) {
                            atomicCourse.set("Software Engineering");
                        }

                        return Mono.empty();
                    }).subscribe();
                }

                return Mono.empty();
            }).subscribe();



            if(atomicModules.get().size() == 0) {
                atomicModules.get().add("This user has not set their modules :( They can do it at <#711344043379916901>");
            }

            atomicModules.get().sort(Comparator.naturalOrder());
            return embedBuilder.createModuleEmbed(atomicModules.get(), atomicCourse.get(), atomicUsername.get(), atomicUrl.get());
        } catch (Exception e) {
            return embedBuilder.constructErrorEmbed();
        }
    }*/
}
