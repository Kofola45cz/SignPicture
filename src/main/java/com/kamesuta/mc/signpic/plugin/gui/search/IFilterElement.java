package com.kamesuta.mc.signpic.plugin.gui.search;

import com.kamesuta.mc.signpic.plugin.SignData;

public interface IFilterElement {
	boolean filter(SignData data);
}