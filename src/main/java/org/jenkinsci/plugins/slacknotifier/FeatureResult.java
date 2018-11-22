package org.jenkinsci.plugins.slacknotifier;

public class FeatureResult {
	final String name;
	final int passPercentage;
	final int totalFailedScenarios;
	final int totalScenarios;

	public FeatureResult(String name, int passPercentage) {
		this(name, passPercentage, 0, 0);
	}

	public FeatureResult(String name, int passPercentage, int totalScenarios, int totalFailedScenarios) {
		this.name = name;
		this.passPercentage = passPercentage;
		this.totalScenarios = totalScenarios;
		this.totalFailedScenarios = totalFailedScenarios;
	}

	public String toString() {
		return this.name + "=" + this.passPercentage;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getFeatureUri() {
		return this.name.replace(".feature", "-feature") + ".html";
	}
	
	public String getDisplayName() {
		return this.name.replaceAll("_", " ").replace(".feature", "");
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