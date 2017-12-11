package ru.alfalab.jenkinsci.plugins;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * @author tolkv
 * @version 10/12/2017
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ExportedBean
public class BakedArtifact {
  private String name;
  private String type;
  private String sha1;
  private String md5;

  @Exported
  public String getName() {
    return name;
  }

  @Exported
  public String getType() {
    return type;
  }

  @Exported
  public String getSha1() {
    return sha1;
  }
}
