package org.jenkinsci.plugins.slacknotifier;

import hudson.Extension;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.model.Job;
import hudson.util.FormValidation;

import java.io.IOException;

import javax.servlet.ServletException;

import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

public class CucumberSlack extends JobProperty<Job<?, ?>> {

	@Override
	public CucumberSlackDescriptor getDescriptor() {
		return (CucumberSlackDescriptor) Jenkins.getInstance().getDescriptor(getClass());
	}

	public static CucumberSlackDescriptor get() {
		return (CucumberSlackDescriptor) Jenkins.getInstance().getDescriptor(CucumberSlack.class);
	}

	@Extension
	public static final class CucumberSlackDescriptor extends JobPropertyDescriptor {

		private String webHookEndpoint;
		
		public CucumberSlackDescriptor() {
			load();
		}

		@Override
		public String getDisplayName() {
			return "Cucumber Slack Notifier";
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
			webHookEndpoint = formData.getString("webHookEndpoint");
			save();
			return super.configure(req, formData);
		}

		public String getWebHookEndpoint() {
			return webHookEndpoint;
		}

		public FormValidation doCheckWebHookEndpoint(@QueryParameter String value) throws IOException, ServletException {
			if (value.length() == 0) {
				return FormValidation.error("Please set a webHookEndpoint");
			}

			if (value.length() < 20) {
				return FormValidation.warning("Isn't the webHookEndpoint too short?");
			}

			if (!value.startsWith("https://hooks.slack.com/")) {
				if (value.contains("/hooks/")) {
					return FormValidation.ok("Endpoint assumed to be a Mattermost endpoint, proper message format will be used");
				} else {
					return FormValidation.warning("Slack endpoint should start with https://hooks.slack.com/");
				}
			}

			return FormValidation.ok();
		}
	}
}
