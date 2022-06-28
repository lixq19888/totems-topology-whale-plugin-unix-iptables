// Generated from com\abtnetworks\totems\whale\plugin\u005Cunix\iptables\Iptables.g4 by ANTLR 4.7.2
package com.abtnetworks.totems.whale.plugin.unix.iptables;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link IptablesParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface IptablesVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link IptablesParser#config}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConfig(IptablesParser.ConfigContext ctx);
	/**
	 * Visit a parse tree produced by {@link IptablesParser#command}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommand(IptablesParser.CommandContext ctx);
	/**
	 * Visit a parse tree produced by {@link IptablesParser#unknownCommand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnknownCommand(IptablesParser.UnknownCommandContext ctx);
	/**
	 * Visit a parse tree produced by {@link IptablesParser#unknownCommandSub}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnknownCommandSub(IptablesParser.UnknownCommandSubContext ctx);
	/**
	 * Visit a parse tree produced by {@link IptablesParser#knownCommand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKnownCommand(IptablesParser.KnownCommandContext ctx);
	/**
	 * Visit a parse tree produced by {@link IptablesParser#linuxIptables}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLinuxIptables(IptablesParser.LinuxIptablesContext ctx);
	/**
	 * Visit a parse tree produced by the {@code linuxIptablesTableHeader}
	 * labeled alternative in {@link IptablesParser#linuxIptablesSub}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLinuxIptablesTableHeader(IptablesParser.LinuxIptablesTableHeaderContext ctx);
	/**
	 * Visit a parse tree produced by the {@code linuxIptablesTableInfo}
	 * labeled alternative in {@link IptablesParser#linuxIptablesSub}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLinuxIptablesTableInfo(IptablesParser.LinuxIptablesTableInfoContext ctx);
	/**
	 * Visit a parse tree produced by the {@code linuxIptablesIgnore}
	 * labeled alternative in {@link IptablesParser#linuxIptablesSub}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLinuxIptablesIgnore(IptablesParser.LinuxIptablesIgnoreContext ctx);
	/**
	 * Visit a parse tree produced by {@link IptablesParser#windowsIptables}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowsIptables(IptablesParser.WindowsIptablesContext ctx);
	/**
	 * Visit a parse tree produced by {@link IptablesParser#windowsIptablesSub}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowsIptablesSub(IptablesParser.WindowsIptablesSubContext ctx);
	/**
	 * Visit a parse tree produced by {@link IptablesParser#address}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddress(IptablesParser.AddressContext ctx);
	/**
	 * Visit a parse tree produced by {@link IptablesParser#text}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitText(IptablesParser.TextContext ctx);
	/**
	 * Visit a parse tree produced by {@link IptablesParser#word}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWord(IptablesParser.WordContext ctx);
	/**
	 * Visit a parse tree produced by {@link IptablesParser#eol}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEol(IptablesParser.EolContext ctx);
	/**
	 * Visit a parse tree produced by {@link IptablesParser#wsBOL}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWsBOL(IptablesParser.WsBOLContext ctx);
}