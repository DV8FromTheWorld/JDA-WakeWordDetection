package net.notfab.voicebot;

import net.dv8tion.ServiceUtil;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.AudioManager;

import javax.security.auth.login.LoginException;

public class VoiceBot extends ListenerAdapter {

    public static void main(String[] args) {
        new VoiceBot();
    }

    public VoiceBot() {
        ServiceUtil.loadServices();
        final String token = System.getProperty("discord.token");
        if (token == null) {
            throw new RuntimeException("-Ddiscord.token was not set!");
        }

        try {
            new JDABuilder(AccountType.BOT)
                    .setToken(token) // Use token provided as JVM argument
                    .addEventListener(this) // Register new MusicBot instance as EventListener
                    .buildAsync(); // Build JDA - connect to discord
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if(!event.getAuthor().getId().equalsIgnoreCase("107562988810027008")) return;
        // This makes sure we only execute our code when someone sends a message with "!play"
        if (!event.getMessage().getContentRaw().startsWith("$test")) return;
        // Now we want to exclude messages from bots since we want to avoid command loops in chat!
        // this will include own messages as well for bot accounts
        // if this is not a bot make sure to check if this message is sent by yourself!
        if (event.getAuthor().isBot()) return;
        Guild guild = event.getGuild();
        // This will get the first voice channel with the name "music"
        // matching by voiceChannel.getName().equalsIgnoreCase("music")
        VoiceChannel channel = guild.getVoiceChannelsByName("Helpers Music", true).get(0);
        AudioManager manager = guild.getAudioManager();

        manager.setReceivingHandler(new VoiceReceiveHandler());
        // Here we finally connect to the target voice channel
        // and it will automatically start pulling the audio from the MySendHandler instance
        manager.openAudioConnection(channel);
    }

}