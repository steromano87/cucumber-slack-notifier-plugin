package org.jenkinsci.plugins.slacknotifier;

public class FeatureResult {
	final String name;
	final int passPercentage;
	final int totalFailedScenarios;
	final int totalScenarios;
	final String uri;

	public FeatureResult(String name, int passPercentage) {
		this(name, passPercentage, 0, 0, name);
	}

	public FeatureResult(String name, int passPercentage, int totalScenarios, int totalFailedScenarios, String uri) {
		this.name = name;
		this.passPercentage = passPercentage;
		this.totalScenarios = totalScenarios;
		this.totalFailedScenarios = totalFailedScenarios;
		this.uri = uri;
	}

	public String toString() {
		return this.name + "=" + this.passPercentage;
	}
	
	public String getName() {
		return this.name;
	}

	public String getUri() {
		return this.uri;
	}
	
	public String getFeatureUri() {
		return this.uri.replace(".feature", "-feature").replace(" ", "-") + ".html";
	}
	
	public String getDisplayName() {
		return this.name.replaceAll("_", " ").replace(" feature$", "");
	}
	
	public int getPassPercentage() {
		return this.passPercentage;
	}

	public int getTotalScenarios() {
		return this.totalScenarios;
	}

	public int getTotalFailedScenarios() {
		return this.totalFailedScenarios;
	}

	public int getTotalPassedScenarios() {
		return this.totalScenarios - this.totalFailedScenarios;
	}
}