package hudson.plugins.git.browser;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.plugins.git.GitChangeSet;
import hudson.plugins.git.GitChangeSet.Path;
import hudson.scm.EditType;
import hudson.scm.RepositoryBrowser;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Git Browser for <a href="http://www.redmine.org/">Redmine</a>.
 * 
 * @author mfriedenhagen
 */
public class RedmineWeb extends GitRepositoryBrowser {

    private static final long serialVersionUID = 1L;

    @DataBoundConstructor
    public RedmineWeb(String repoUrl) {
        super(repoUrl);
    }

    @Override
    public URL getChangeSetLink(GitChangeSet changeSet) throws IOException {
        URL url = getUrl();
        return new URL(url, "diff?rev=" + changeSet.getId());
    }

    /**
     * Creates a link to the file diff.
     * 
     * https://SERVER/PATH/projects/PROJECT/repository/revisions/a9182a07750c9a0dfd89a8461adf72ef5ef0885b/diff/pom.xml
     * 
     * Returns a diff link for {@link EditType#DELETE} and {@link EditType#EDIT}, for {@link EditType#ADD} returns an
     * {@link #getFileLink}.
     * 
     * 
     * @param path
     *            affected file path
     * @return diff link
     * @throws IOException on input or output error
     */
    @Override
    public URL getDiffLink(Path path) throws IOException {
        final GitChangeSet changeSet = path.getChangeSet();
        URL url = getUrl();
        final URL changeSetLink = new URL(url, "revisions/" + changeSet.getId());
        final URL difflink;
        if (path.getEditType().equals(EditType.ADD)) {
            difflink = getFileLink(path);
        } else {
            difflink = new URL(changeSetLink, changeSetLink.getPath() + "/diff/" + path.getPath());
        }
        return difflink;
    }

    /**
     * Creates a link to the file.
     * https://SERVER/PATH/projects/PROJECT/repository/revisions/a9182a07750c9a0dfd89a8461adf72ef5ef0885b/entry/pom.xml
     * For deleted files just returns a diff link, which will have /dev/null as target file.
     * 
     * @param path affected file path
     * @return file link
     * @throws IOException on input or output error
     */
    @Override
    public URL getFileLink(Path path) throws IOException {
        if (path.getEditType().equals(EditType.DELETE)) {
            return getDiffLink(path);
        } else {
            final String spec = "revisions/" + path.getChangeSet().getId() + "/entry/" + path.getPath();
            URL url = getUrl();
            return new URL(url, url.getPath() + spec);
        }
    }

    @Extension
    public static class RedmineWebDescriptor extends Descriptor<RepositoryBrowser<?>> {
        public String getDisplayName() {
            return "redmineweb";
        }

        @Override
        public RedmineWeb newInstance(StaplerRequest req, JSONObject jsonObject) throws FormException {
            return req.bindJSON(RedmineWeb.class, jsonObject);
        }
    }

}
