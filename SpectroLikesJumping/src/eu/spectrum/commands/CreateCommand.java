package eu.spectrum.commands;

import static eu.spectrum.listeners.CreationListener.creationMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.sk89q.worldedit.Vector;

import eu.spectrum.main.Main;
import eu.spectrum.main.Systems;
import eu.spectrum.utils.Difficulty;
import eu.spectrum.utils.ModuleData;
import eu.spectrum.utils.ModuleManager;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.ChatMessage;
import net.minecraft.server.v1_8_R3.ContainerAnvil;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutOpenWindow;

@SuppressWarnings("deprecation")
public class CreateCommand implements CommandExecutor {

	public static final String setCommand = "/module set";
	public static final String removeCommand = "/module reset";

	@Override
	public boolean onCommand(CommandSender sender, Command command, String name, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (p.isOp()) {
				if (args.length < 1) {
					p.sendMessage(Main.PREFIX + Main.handler.format("cmd.create.desc"));
					return false;
				}
				if (args[0].equalsIgnoreCase("load")) {
					if (args.length < 2) {
						p.sendMessage(Main.PREFIX + Main.handler.format("module.name-missing"));
						return false;
					}
					String moduleName = assembleArg(1, args);
					if (ModuleManager.isModule(moduleName)) {
						ModuleData data = ModuleManager.getModule(moduleName);
						Location nLoc = new Location(p.getWorld(), p.getLocation().getX(), p.getLocation().getY(),
								p.getLocation().getZ(), data.start.getYaw(), data.start.getPitch());
						p.teleport(nLoc);
						ModuleManager.paste(p.getLocation(), assembleArg(1, args));
					} else
						p.sendMessage(Main.PREFIX + Main.handler.format("module.absent"));
				} else if (args[0].equalsIgnoreCase("add")) {
					if (args.length >= 2 && args[1].startsWith("reset")) {
						if (creationMode.containsKey(p)) {
							p.playSound(p.getLocation(), Sound.BURP, 1, 1);
							creationMode.get(p).resetLocs();
							step(p);
						}
						return false;
					}
					if (!creationMode.containsKey(p)) {
						creationMode.put(p, new ModuleData());
						step(p);
					} else {
						p.sendMessage(" ");
						p.sendMessage(Main.PREFIX + Main.handler.format("cmd.create.leave"));
						p.sendMessage(" ");
						p.playSound(p.getLocation(), Sound.BURP, 1, 1);
						ModuleData data = creationMode.remove(p);
						if ((data.getStart() != null || data.tmpStart != null) && data.loadedClipboard != null) {
							data.remove(data.getStart() == null ? data.tmpStart : data.getStart());
						}
					}
				} else if (args[0].endsWith("set")) {
					if (args.length >= 2) {
						if (!creationMode.containsKey(p))
							creationMode.put(p, new ModuleData());
						ModuleData currData = creationMode.get(p);

						if (args[0].startsWith("re")) {
							if (currData.setField(args[1], null)) {
								p.sendMessage("deleted");
								step(p);
								return false;
							}
						} else {
							if (currData.allSet()) {
								p.sendMessage(Main.PREFIX + Main.handler.format("cmd.create.all-set"));
								return false;
							}
							if (currData.getFieldByName(args[1]) != null
									&& currData.getFieldByName(args[1]).needsPlate) {
								if (!Systems.isCheckpoint(p.getLocation())) {
									p.sendMessage(Main.PREFIX + Main.handler.format("cmd.create.plate-missing"));

									return false;
								}
							}
							if (!currData.locExists(p.getLocation())) {
								if (currData.setField(args[1], p.getLocation())) {
									step(p);
									return false;
								}
							} else {
								p.sendMessage(Main.PREFIX + Main.handler.format("module.same-location"));
								return false;
							}
						}
						p.sendMessage(Main.PREFIX + Main.handler.format("field.absent"));
					} else
						p.sendMessage(Main.PREFIX + Main.handler.format("field.missing"));
				} else if (args[0].equalsIgnoreCase("delete")) {
					if (args.length >= 2) {
						String moduleName = assembleArg(1, args);
						if (ModuleManager.delete(moduleName))
							p.sendMessage(Main.PREFIX + Main.handler.format("module.delete.succeeded"));
						else
							p.sendMessage(Main.PREFIX + Main.handler.format("module.delete.failed"));

					} else {
						p.sendMessage(Main.PREFIX + Main.handler.format("name.missing"));
					}
				} else if (args[0].equalsIgnoreCase("list")) {
					List<ModuleData> modules = ModuleManager.loadModules();
					if (modules.size() <= 0) {
						p.sendMessage(Main.PREFIX + Main.handler.format("module.add"));
						return false;
					}
					final int maxList = 5;
					try {
						int page = args.length > 1 ? Integer.parseInt(args[1]) : 1;
						page -= 1;
						if (page < 0)
							page = 0;

						int pages = (int) Math.ceil((double) modules.size() / (double) maxList);

						if (page > pages - 1)
							page = pages - 1;

						p.sendMessage("§8§m---------§6[" + Main.handler.format("modules") + "]§8§m----------§r\n \n");

						for (int i = page * maxList; i < (page * maxList) + maxList; i++) {
							if (i <= modules.size() - 1) {
								ModuleData module = modules.get(i);
								Vector size = module.loadedClipboard.getSize();

								TextComponent text = new TextComponent(
										module.difficulty.getChatColor() + "  §l» " + module.name);
								TextComponent hoverText = new TextComponent(
										Main.handler.format("module.spawn", module.name));
								HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
										new BaseComponent[] { hoverText });
								text.setHoverEvent(hoverEvent);
								text.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/module load " + module.name));

								TextComponent delete = new TextComponent(" §7[§c§l🗑§7]");

								// TODO: delete comp, filler comp with two spaces, inventory with 1 column and wool to
								// accept(hashmap,listener)

								TextComponent edit = new TextComponent(" §7[§a§l✎§7]");
								edit.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/module edit " + module.name));
								TextComponent hoverEdit = new TextComponent(Main.handler.format("module.edit"));
								HoverEvent hoverEditEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
										new BaseComponent[] { hoverEdit });
								edit.setHoverEvent(hoverEditEvent);
								text.addExtra(edit);

								p.spigot().sendMessage(text);

								String restMessage = "       §r§7-> " + Main.handler.format("difficulty") + ": "
										+ module.difficulty.getChatColor() + module.difficulty.getName() + "\n"
										+ "       §7-> " + Main.handler.format("volume") + ": §8"
										+ (size.getBlockX() * size.getBlockY() * size.getBlockZ()) + " "
										+ Main.handler.format("blocks") + "\n \n";

								p.sendMessage(restMessage);
							}
						}

						TextComponent defFiller = new TextComponent("§8§m-----------");
						TextComponent previous = new TextComponent("§6[" + (page + 1) + "§7/");
						previous.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/module list " + (page)));
						TextComponent previousHover = new TextComponent("§c" + Main.handler.format("backwards"));
						previous.setHoverEvent(
								new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] { previousHover }));
						TextComponent next = new TextComponent("§6" + pages + "]");
						next.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/module list " + (page + 2)));
						TextComponent nextHover = new TextComponent("§a" + Main.handler.format("forwards"));
						next.setHoverEvent(
								new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] { nextHover }));

						TextComponent base = (TextComponent) defFiller.duplicate();
						base.addExtra(previous);
						base.addExtra(next);
						base.addExtra(defFiller.duplicate());

						p.spigot().sendMessage(base);
					} catch (Exception ex) {
						p.sendMessage(Main.PREFIX + Main.handler.format("cmd.create.use-number"));
						ex.printStackTrace();
					}
				} else if (args[0].equalsIgnoreCase("edit")) {
					String modName = assembleArg(1, args);
					if (ModuleManager.isModule(modName)) {
						creationMode.put(p, ModuleManager.getModule(modName));
						openInv(p, true);

					} else {
						p.sendMessage(Main.PREFIX + Main.handler.format("module.absent"));
					}

				}
			}
		}
		return false;
	}

	public static String assembleArg(int startIndex, String[] args) {
		String str = "";
		for (int i = startIndex; i < args.length; i++) {
			str += args[i] + (i < args.length - 1 ? " " : "");
		}
		return str;
	}

	public static final class FakeAnvil extends ContainerAnvil {

		public FakeAnvil(EntityHuman entityHuman) {
			super(entityHuman.inventory, entityHuman.world, new BlockPosition(0, 0, 0), entityHuman);
		}

		@Override
		public boolean a(EntityHuman entityHuman) {
			return true;
		}
	}

	public static Map<Player, Map<Integer, ItemStack>> itemCache = new HashMap<>();

	public static void step(Player p) {
		p.playSound(p.getLocation(), Sound.ANVIL_BREAK, 1, 1);
		printCreationStatus(p);
		ModuleData data = creationMode.get(p);
		if (data.getEnd() != null && data.getStart() != null && data.getLoc1() != null && data.getLoc2() != null) {
			if (data.name == null) {
				openInv(p, false);
			} else {
				ModuleManager.registerModule(p, data);
				p.sendMessage(Main.PREFIX + Main.handler.format("construct.apply-changes"));
			}
		}
	}

	public static void openInv(Player p, boolean change) {

		ModuleData data = creationMode.get(p);

		EntityPlayer entityPlayer = ((CraftPlayer) p).getHandle();
		FakeAnvil fakeAnvil = new FakeAnvil(entityPlayer);
		int containerId = entityPlayer.nextContainerCounter();

		((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutOpenWindow(containerId,
				"minecraft:anvil", new ChatMessage(Main.handler.format("module.info"), new Object[] {}), 0));

		entityPlayer.activeContainer = fakeAnvil;
		entityPlayer.activeContainer.windowId = containerId;
		entityPlayer.activeContainer.addSlotListener(entityPlayer);
		entityPlayer.activeContainer = fakeAnvil;
		entityPlayer.activeContainer.windowId = containerId;

		Inventory inv = fakeAnvil.getBukkitView().getTopInventory();
		ItemStack paper = new ItemStack(Material.PAPER);
		ItemMeta meta = paper.getItemMeta();
		meta.setDisplayName(data.name == null ? Systems.defModuleName : data.name);
		paper.setItemMeta(meta);
		inv.setItem(0, paper);

		HashMap<Integer, ItemStack> cache = new HashMap<>();

		Inventory playerInv = p.getInventory();
		for (int i = 0; i < playerInv.getSize(); i++) {
			ItemStack stack = playerInv.getItem(i);
			cache.put(i, stack);
		}
		itemCache.put(p, cache);
		playerInv.clear();

		ItemStack arrow = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
		SkullMeta skullMeta = (SkullMeta) arrow.getItemMeta();
		skullMeta.setOwner("MHF_ArrowRight");
		skullMeta.setDisplayName("§c§l" + Main.handler.format("difficulties"));
		arrow.setItemMeta(skullMeta);

		playerInv.setItem(20, arrow);

		ItemStack tickbox = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
		SkullMeta tickMeta = (SkullMeta) tickbox.getItemMeta();
		tickMeta.setOwner("MHF_youtube");
		tickMeta.setDisplayName("§a§l" + Main.handler.format("complete"));
		tickbox.setItemMeta(tickMeta);

		playerInv.setItem(change ? 3 : 4, tickbox);

		if (change) {
			ItemStack changeBuild = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
			SkullMeta buildMeta = (SkullMeta) changeBuild.getItemMeta();
			buildMeta.setOwner("MHF_cam");
			buildMeta.setDisplayName(Main.handler.format("construct.change"));
			changeBuild.setItemMeta(buildMeta);

			playerInv.setItem(5, changeBuild);
		}

		setDifficultyPart(data.difficulty, playerInv);

	}

	public static void setDifficultyPart(Difficulty difficulty, Inventory playerInv) {

		for (int i = 0; i < playerInv.getSize(); i++) {
			if (playerInv.getItem(i) == null || playerInv.getItem(i).getType() == Material.STAINED_GLASS_PANE) {
				playerInv.setItem(i, paneFiller((byte) 7, "§8-"));
			}
		}

		int count = 21;
		for (Difficulty d : Difficulty.values()) {
			ItemStack wool = new ItemStack(Material.WOOL, 1, (byte) d.getSubColorID());

			ItemMeta woolMeta = wool.getItemMeta();
			woolMeta.setDisplayName(d.getChatColor() + d.getName());
			wool.setItemMeta(woolMeta);

			playerInv.setItem(count, wool);

			if (d == difficulty) {
				playerInv.setItem(count - 9, paneFiller((byte) d.getSubColorID(), "§8-"));
				playerInv.setItem(count + 9, paneFiller((byte) d.getSubColorID(), "§8-"));
			}
			count++;
		}
	}

	public static ItemStack paneFiller(byte color, String name) {
		ItemStack pane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) color);
		ItemMeta meta = pane.getItemMeta();
		meta.setDisplayName(name);
		pane.setItemMeta(meta);
		return pane;
	}

	public static void printCreationStatus(Player p) {
		if (creationMode.containsKey(p)) {
			p.sendMessage("\n \n§8§m---------§a§l[" + Main.handler.format("state") + "]§8§m----------§r\n \n");

			for (TextComponent comp : creationMode.get(p).toStates()) {
				p.spigot().sendMessage(comp);
			}
			p.sendMessage("\n \n");

			TextComponent fillerDef = new TextComponent("§8§m----");

			TextComponent cancel = new TextComponent("§7[§4§l" + Main.handler.format("cancel") + "§7]");
			cancel.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/module add"));
			TextComponent cancelHover = new TextComponent(Main.handler.format("cmd.create.cancel-registration"));
			cancel.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] { cancelHover }));

			TextComponent reset = new TextComponent(" §7[§c§l" + Main.handler.format("reset") + "§r§7]");
			reset.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/module add reset"));
			TextComponent hover = new TextComponent(Main.handler.format("cmd.create.reset-registration"));
			reset.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] { hover }));

			TextComponent message = (TextComponent) fillerDef.duplicate();
			message.addExtra(cancel);
			message.addExtra(reset);
			message.addExtra(fillerDef);

			p.spigot().sendMessage(message);
		}
	}

}
