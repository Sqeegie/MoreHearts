package com.sqeegie.mh.configuration;

import java.util.UUID;

public class MoreHeartsPlayer {
	
	public UUID uuid;
	public String lastUsername;
	public double health;
	public double extraHearts;
	
	public MoreHeartsPlayer(UUID uuid, String lastUsername, Double health, Double extraHearts) {
		this.uuid = uuid;
		this.lastUsername = lastUsername;
		this.health = health;
		this.extraHearts = extraHearts;
	}
}
