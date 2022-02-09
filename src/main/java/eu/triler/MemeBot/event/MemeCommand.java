package eu.triler.MemeBot.event;

import java.awt.Color;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import eu.triler.MemeBot.MemeBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MemeCommand implements EventListener 
{
	@Override
	public void onEvent(GenericEvent event)
	{
		if(event instanceof GuildMessageReceivedEvent) 
		{
			send((GuildMessageReceivedEvent) event);
		}
	}

	private void send(GuildMessageReceivedEvent event)
	{
		if(event.getMessage().getContentRaw().toLowerCase().equals(MemeBot.Config.GetString("command"))) {
			OkHttpClient client = new OkHttpClient();

			Request request = new Request.Builder().url("https://meme-api.herokuapp.com/gimme/1").get().build();

			try {
				Response response = client.newCall(request).execute();
				String message = response.body().string();
				int index1 = message.indexOf("\"url\":\"");			
				String sub = message.substring(index1+7);
				int index2 = sub.indexOf("\"");
				String url = sub.substring(0,index2);

				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss");  
				LocalDateTime now = LocalDateTime.now();  

				EmbedBuilder embed =  new EmbedBuilder();
				embed.setTitle(event.getGuild().getName()+" | Meme");
				embed.setColor(new Color(186,85,211));
				embed.setImage(url);
				embed.setDescription(event.getAuthor().getAsMention()+" I have something for you. Hope you like it.");
				embed.setFooter(dtf.format(now), event.getGuild().getIconUrl());

				event.getChannel().sendMessage(embed.build()).queue();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}