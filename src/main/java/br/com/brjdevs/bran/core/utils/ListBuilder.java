package br.com.brjdevs.bran.core.utils;

import lombok.NonNull;

import java.util.List;
import java.util.stream.Collectors;

public class ListBuilder {
	
	private List<String> list;
	private int page;
	private int itemsShown;
	private String listName;
	private String footer;
	
	public ListBuilder(List<String> list, int page, int itemsShown) {
		this.list = list;
		if (page != 0) this.page = page; else this.page = 1;
		this.itemsShown = itemsShown;
		this.listName = "";
		this.footer = "";
	}
	public ListBuilder setName(String name) {
		this.listName = name;
		return this;
	}
	public ListBuilder setFooter(String footer) {
		this.footer = footer;
		return this;
	}
	public int getMaxPages() {
		if (list.size() % itemsShown == 0) return list.size() / itemsShown;
		return (list.size() / itemsShown) + 1;
	}
	public String format(@NonNull Format format, String... markdown) {
		int min = (page * itemsShown) - itemsShown;
		int max = page * itemsShown;
		switch (format) {
			case SIMPLE:
				return String.format(format.toString(), listName, page, getMaxPages(), String.join("\n", list.stream().filter(item -> list.indexOf(item) < max && list.indexOf(item) >= min).map(item -> (list.indexOf(item) + 1) + ". " + item).collect(Collectors.toList())), footer);
			case CODE_BLOCK:
				return String.format(format.toString(), markdown[0], listName, page, getMaxPages(), String.join("\n", list.stream().filter(item -> list.indexOf(item) < max && list.indexOf(item) >= min).map(item -> (list.indexOf(item) + 1) + ". " + item).collect(Collectors.toList())), footer);
			default:
				return String.format(Format.NONE.toString(), String.join("\n", String.join("\n", list.stream().filter(item -> list.indexOf(item) < max && list.indexOf(item) >= min).collect(Collectors.toList()))));
		}
	}
	
	public enum Format {
		NONE("%s"),
		SIMPLE("%s - Page %d/%dn\n%s"),
		CODE_BLOCK("```%s\n# %s - Page %d/%d\n%s\n# %s\n```");
		private String str;
		Format(String str) {
			this.str = str;
		}
		public String toString() {
			return str;
		}
	}
}
