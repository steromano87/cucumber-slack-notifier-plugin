package org.jenkinsci.plugins.slacknotifier;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class CucumberResult {
	final List<FeatureResult> featureResults;
	final int passPercentage;
	final int totalScenarios;
	
	public CucumberResult(List<FeatureResult> featureResults, int totalScenarios, int passPercentage) {
		this.featureResults = featureResults;
		this.totalScenarios = totalScenarios;
		this.passPercentage = passPercentage;
	}
	
	public int getPassPercentage() {
		return this.passPercentage;
	}
	
	public int getTotalFeatures() {
		return this.featureResults.size();
	}
	
	public int getTotalScenarios() {
		return this.totalScenarios;
	}
	
	public List<FeatureResult> getFeatureResults() {
		return this.featureResults;
	}
	
	public String toSlackMessage(final String jobName,
			final int buildNumber, final String channel, final String jenkinsUrl, final String buildUrl) {
		final JsonObject json = new JsonObject();
		json.addProperty("channel", "#" + channel);
		addCaption(json, buildNumber, jobName, jenkinsUrl, buildUrl);
		json.add("fields", getFields(jobName, buildNumber, jenkinsUrl));

		if (getPassPercentage() == 100) {
			addColourAndIcon(json, "good", ":thumbsup:");
		} else if (getPassPercentage() >= 98) {
			addColourAndIcon(json, "warning", ":hand:");
		} else {
			addColourAndIcon(json, "danger", ":thumbsdown:");
		}

		json.addProperty("username", jobName);
		return json.toString();
	}

	public String toMattermostMessage(final String jobName,
			final int buildNumber, final String channel, final String jenkinsUrl, final String buildUrl) {
		final JsonObject json = new JsonObject();
		json.addProperty("channel", "#" + channel);
		json.addProperty("text", this.buildResultsTable(jobName, jenkinsUrl, buildNumber, buildUrl));
		json.addProperty("username", jobName);
		return json.toString();
	}

	private String getJenkinsHyperlink(final String jenkinsUrl, final String jobName, final int buildNumber) {
		StringBuilder s = new StringBuilder();
		s.append(jenkinsUrl);
		if (!jenkinsUrl.trim().endsWith("/")) {
			s.append("/");
		}
		s.append("job/");
		s.append(jobName);
		s.append("/");
		s.append(buildNumber);
		s.append("/");
		return s.toString();
	}
	
	public String toHeader(final String jobName, final int buildNumber, final String jenkinsUrl, final String buildUrl) {
		StringBuilder s = new StringBuilder();
		s.append("Features: ");
		s.append(getTotalFeatures());
		s.append(", Scenarios: ");
		s.append(getTotalScenarios());
		s.append(", Build: <");
		s.append(buildUrl);
		s.append(String.format("cucumber-html-reports/|%d", buildNumber));
		s.append(">");
		return s.toString();
	}
	
	private void addCaption(final JsonObject json, final int buildNumber, final String jobName, final String jenkinsUrl, final String buildUrl) {
		json.addProperty("pretext", toHeader(jobName, buildNumber, jenkinsUrl, buildUrl));
	}
	
	private void addColourAndIcon(JsonObject json, String good, String value) {
		json.addProperty("color", good);
		json.addProperty("icon_emoji", value);
	}

	private JsonArray getFields(final String jobName, final int buildNumber, final String jenkinsUrl) {
		final String hyperLink = getJenkinsHyperlink(jenkinsUrl, jobName, buildNumber) + "cucumber-html-reports/";
		final JsonArray fields = new JsonArray();
		fields.add(shortTitle("Features"));
		fields.add(shortTitle("Pass %"));
		for (FeatureResult feature : getFeatureResults()) {
			final String featureDisplayName = feature.getDisplayName();
			final String featureFileName = feature.getFeatureUri();
			fields.add(shortObject("<" + hyperLink + featureFileName + "|" + featureDisplayName + ">"));
			fields.add(shortObject(feature.getPassPercentage() + " %"));
		}
		fields.add(shortObject("-------------------------------"));
		fields.add(shortObject("-------"));
		fields.add(shortObject("Total Passed"));
		fields.add(shortObject(getPassPercentage() + " %"));
		return fields;
	}


	private String buildResultsTable(final String jobName, final String jenkinsUrl, final int buildNumber, final String buildUrl) {
		final String hyperLink = jenkinsUrl + buildUrl + "cucumber-html-reports/report-feature_";
		StringBuilder buffer = new StringBuilder();

		// Start by writing jobname and build number in message header
		buffer.append(String.format("#### Test results for job \"%s\", build [#%d](%s) ", jobName, buildNumber, jenkinsUrl + buildUrl));
		if (getPassPercentage() == 100) {
			buffer.append(":white_check_mark:");
		} else {
			buffer.append(":x:");
		}
		buffer.append("\n");

		// Add the detail for the total scenarios
		buffer.append(String.format("Total features: %d\n", this.getFeatureResults().size()));
		buffer.append(String.format("Total scenarios: %d\n\n", this.totalScenarios));

		// Add the top table headers
		buffer.append("| Features     | Passed            | Failed       | Status |\n");
		buffer.append("| ------------ | :---------------: | :----------: | :---:  |\n");

		// Iterate over features and build results matrix
        int overallPassed = 0;
        int overallFailed = 0;

		for (FeatureResult feature : getFeatureResults()) {
			buffer.append(String.format("| [%s](%s)", feature.getDisplayName(), hyperLink + feature.getFeatureUri()));
			buffer.append(String.format("| %d%% (%d/%d) ", feature.getPassPercentage(), feature.getTotalPassedScenarios(), feature.getTotalScenarios()));
			buffer.append(String.format("| %d%% (%d/%d) ", 100 - feature.getPassPercentage(), feature.getTotalFailedScenarios(), feature.getTotalScenarios()));
			if (feature.getPassPercentage() == 100) {
				buffer.append("| :white_check_mark: |\n");
			} else if (feature.getPassPercentage() != 100 && feature.getTotalFailedScenarios() != feature.totalScenarios) {
				buffer.append("| :warning: |\n");
			} else {
				buffer.append("| :x: |\n");
			}

			// Update the overall counters
            overallFailed += feature.getTotalFailedScenarios();
			overallPassed += feature.getTotalPassedScenarios();
		}

		// End the table with the overall result
        buffer.append(String.format(
                "| *Total*   | *%d%% (%d/%d)*   | *%d%% (%d/%d)*   |  -  |\n",
                getPassPercentage(), overallPassed, this.totalScenarios,
                100 - getPassPercentage(), overallFailed, this.totalScenarios
        ));

		return buffer.toString();
	}

	
	private JsonObject shortObject(final String value) {
		JsonObject obj = new JsonObject();
		obj.addProperty("value", value);
		obj.addProperty("short", true);
		return obj;
	}

	private JsonObject shortTitle(final String title) {
		JsonObject obj = new JsonObject();
		obj.addProperty("title", title);
		obj.addProperty("short", true);
		return obj;
	}
}