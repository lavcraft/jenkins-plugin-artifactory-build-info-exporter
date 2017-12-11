package ru.alfalab.jenkinsci.plugins;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import hudson.model.Action;
import lombok.*;
import org.jfrog.build.api.Artifact;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.List;
import java.util.Map;

/**
 * @author tolkv
 * @version 08/12/2017
 */
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@ExportedBean(defaultVisibility = 999)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
public class ArtifactInfoData implements Action {
  private Map<String, List<BakedArtifact>> artifactsByType;

  @Exported
  public Map<String, List<BakedArtifact>> getArtifactsByType() {
    return artifactsByType;
  }

  @Override
  public String getIconFileName() {
    return null;
  }

  @Override
  public String getDisplayName() {
    return null;
  }

  @Override
  public String getUrlName() {
    return null;
  }
}
