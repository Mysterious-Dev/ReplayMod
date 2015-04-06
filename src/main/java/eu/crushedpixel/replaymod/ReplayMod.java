package eu.crushedpixel.replaymod;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import eu.crushedpixel.replaymod.api.client.ApiClient;
import eu.crushedpixel.replaymod.api.client.ApiException;
import eu.crushedpixel.replaymod.events.GuiEventHandler;
import eu.crushedpixel.replaymod.events.GuiReplayOverlay;
import eu.crushedpixel.replaymod.events.KeyInputHandler;
import eu.crushedpixel.replaymod.events.RecordingHandler;
import eu.crushedpixel.replaymod.events.TickAndRenderListener;
import eu.crushedpixel.replaymod.gui.GuiReplaySaving;
import eu.crushedpixel.replaymod.online.authentication.AuthenticationHandler;
import eu.crushedpixel.replaymod.recording.ConnectionEventHandler;
import eu.crushedpixel.replaymod.registry.KeybindRegistry;
import eu.crushedpixel.replaymod.registry.LightingHandler;
import eu.crushedpixel.replaymod.renderer.SafeEntityRenderer;
import eu.crushedpixel.replaymod.settings.ReplaySettings;

@Mod(modid = ReplayMod.MODID, version = ReplayMod.VERSION)
public class ReplayMod
{

	//TODO: Set ReplayHandler replaying to false when replay is exited
	//TODO: Hide Titles upon hurrying
	//TODO: Override Enchantment Rendering for items when replaying (to adjust speed of animation)

	//TODO: Show the player whether he has already uploaded a replay

	//TODO: Hinting to the b/v key feature

	//XXX
	//Known Bugs
	//
	//Keyframes have problems with Linear Paths
	//Rain isn't working
	//Incompatible with Shaders Mod
	//
	//

	public static final String MODID = "replaymod";
	public static final String VERSION = "0.0.1";

	private static final Minecraft mc = Minecraft.getMinecraft();

	public static GuiReplayOverlay overlay = new GuiReplayOverlay();

	public static ReplaySettings replaySettings;
	public static Configuration config;

	public static boolean firstMainMenu = true;

	public static RecordingHandler recordingHandler;

	public static int TP_DISTANCE_LIMIT = 128;

	public static final ApiClient apiClient = new ApiClient();

	// The instance of your mod that Forge uses.
	@Instance(value = "ReplayModID")
	public static ReplayMod instance;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();

		replaySettings = new ReplaySettings();
		replaySettings.readValues();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(new ConnectionEventHandler());
		MinecraftForge.EVENT_BUS.register(new GuiEventHandler());

		FMLCommonHandler.instance().bus().register(new KeyInputHandler());

		recordingHandler = new RecordingHandler();
		FMLCommonHandler.instance().bus().register(recordingHandler);
		MinecraftForge.EVENT_BUS.register(recordingHandler);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		overlay = new GuiReplayOverlay();
		FMLCommonHandler.instance().bus().register(overlay);
		MinecraftForge.EVENT_BUS.register(overlay);

		TickAndRenderListener tarl = new TickAndRenderListener();
		FMLCommonHandler.instance().bus().register(tarl);
		MinecraftForge.EVENT_BUS.register(tarl);

		KeybindRegistry.initialize();

		try {
			mc.entityRenderer = new SafeEntityRenderer(mc, mc.entityRenderer);
		} catch(Exception e) {
			e.printStackTrace();
		}	

		//clean up replay_recordings folder
		removeTmcprFiles();

		/*
		boolean auth = false;
		try {
			auth = AuthenticationHandler.hasDonated(Minecraft.getMinecraft().getSession().getPlayerID());
		} catch(Exception e) {
			JOptionPane.showMessageDialog(null, "Couldn't connect to the Replay Mod Server to verify whether you donated!");
			FMLCommonHandler.instance().exitJava(0, false);
		}

		if(!auth) {
			JOptionPane.showMessageDialog(null, "It seems like you didn't donate, so you can't use the Replay Mod yet.");
			FMLCommonHandler.instance().exitJava(0, false);
		}
		 */
	}

	private void removeTmcprFiles() {
		File folder = new File("./replay_recordings/");
		folder.mkdirs();

		for(File f : folder.listFiles()) {
			if(("."+FilenameUtils.getExtension(f.getAbsolutePath())).equals(ConnectionEventHandler.TEMP_FILE_EXTENSION)) {
				f.delete();
			}
		}
	}
}
