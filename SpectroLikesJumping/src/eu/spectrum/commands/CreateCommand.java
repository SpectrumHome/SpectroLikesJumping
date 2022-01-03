package eu.spectrum.commands;

import static eu.spectrum.listeners.CreationListener.creationMode;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldedit.Vector;

import eu.spectrum.main.Main;
import eu.spectrum.main.Systems;
import eu.spectrum.utils.Difficulty;
import eu.spectrum.utils.ModuleData;
import eu.spectrum.utils.ModuleManager;
import eu.spigotui.ui.SpigotUI;
import eu.spigotui.ui.components.UIButton;
import eu.spigotui.ui.components.UIDisplayComponent;
import eu.spigotui.ui.top.TextFieldInventory;
import eu.spigotui.utils.ItemBuilder;
import eu.spigotui.utils.UISection;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

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

								TextComponent base = new TextComponent("  ");

								TextComponent delete = new TextComponent("§7[§c§l✘§7]");
								delete.setClickEvent(
										new ClickEvent(Action.RUN_COMMAND, "/module delete " + module.name));
								TextComponent hoverDelete = new TextComponent("§c" + module.name + " löschen");
								delete.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
										new BaseComponent[] { hoverDelete }));

								base.addExtra(delete);
								// TODO: delete comp, filler comp with two spaces, inventory with 1 column and
								// wool to
								// accept(hashmap,listener)

								TextComponent edit = new TextComponent(" §7[§a§l✎§7]");
								edit.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/module edit " + module.name));
								TextComponent hoverEdit = new TextComponent(Main.handler.format("module.edit"));
								HoverEvent hoverEditEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
										new BaseComponent[] { hoverEdit });
								edit.setHoverEvent(hoverEditEvent);
								base.addExtra(edit);
								text.addExtra(base);

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

		ItemStack arrow = new ItemBuilder(Material.SKULL_ITEM).setOwner("MHF_ArrowRight")
				.setName("§c§l" + Main.handler.format("difficulties")).build();
		ItemStack tickbox = new ItemBuilder(Material.SKULL_ITEM).setOwner("MHF_youtube")
				.setName("§a§l" + Main.handler.format("complete")).build();
		ItemStack changeBuild = new ItemBuilder(Material.SKULL_ITEM).setOwner("MHF_cam")
				.setName("§a§l" + Main.handler.format("construct.change")).build();

		SpigotUI ui = new SpigotUI(p);
		TextFieldInventory field = new TextFieldInventory(data.name == null ? Systems.defModuleName : data.name);
		ui.setActiveInventory(field);

		ui.addComponent(UISection.BOTTOM, 0, 0, new UIDisplayComponent(ItemBuilder.paneFiller(7, "§8-"), 9, 4));

		ui.addComponent(UISection.BOTTOM, 2, 1, new UIDisplayComponent(arrow));
		ui.addComponent(UISection.BOTTOM, change ? 3 : 4, 3, new UIButton(tickbox).setOnClick((action) -> {

		}));

		if (change) {
			ui.addComponent(UISection.BOTTOM, 5, 3, new UIButton(changeBuild).setOnClick((action) -> {

			}));
		}

		int count = 0;
		for (Difficulty d : Difficulty.values()) {
			ItemStack dif = new ItemBuilder(Material.WOOL).setDamage(d.getSubColorID())
					.setName(d.getChatColor() + d.getName()).build();

			if (d == data.difficulty)
				ui.addComponent(UISection.BOTTOM, count + 3, 0, new UIDisplayComponent(
						ItemBuilder.paneFiller(d.getSubColorID(), d.getChatColor() + d.getName()),1,3));
			
			

			ui.addComponent(UISection.BOTTOM,count+ 3, 1, new UIButton(dif).setOnClick((action) -> {

			}));
			count++;

		}
		
		ui.openInventory();
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
