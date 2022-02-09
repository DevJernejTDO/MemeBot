package eu.triler.MemeBot;

import javax.security.auth.login.LoginException;

import eu.triler.MemeBot.event.MemeCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class MemeBot {

	public static JDA jda;
	public static ConfigYML Config;	

	public static void main(String[] args) throws LoginException, InterruptedException 
	{

		Config = new ConfigYML();
		Config.CopyDefault();


		jda = JDABuilder.createDefault(Config.GetString("token"), GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES).build();
		jda.awaitReady();

		jda.addEventListener(new MemeCommand());
	}
}
