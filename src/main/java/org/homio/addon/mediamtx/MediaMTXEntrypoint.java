package org.homio.addon.mediamtx;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.homio.api.AddonEntrypoint;
import org.homio.api.Context;
import org.homio.api.ContextMediaVideo;
import org.homio.api.Unregistered;
import org.homio.api.model.OptionModel;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class MediaMTXEntrypoint implements AddonEntrypoint {

  private final Context context;
  private final List<Unregistered> providerUnregistered = new ArrayList<>();

  @Override
  public void init() {
    MediaMTXEntity entity = MediaMTXEntity.getEntity(context);
    context.event().addEntityStatusUpdateListener(entity.getEntityID(), "mediamdx", baseEntityIdentifier -> {
      if (entity.getStatus().isOnline()) {
        providerUnregistered.add(context.media().video().addVideoWebRTCProvider("mediamdx", entity.getWebRtcPort()));
        providerUnregistered.add(context.media().video().addVideoSourceInfoListener("mediamdx", new ContextMediaVideo.VideoSourceInfoListener() {
          @Override
          public void addVideoSourceInfo(String path, Map<String, OptionModel> videoSources) {
            entity.getService().addSourceInfo(path, videoSources);
          }

          @Override
          public void removeVideoSourceInfo(String path) {
          }
        }));
        providerUnregistered.add(context.media().video().addVideoSourceListener("mediamdx", new ContextMediaVideo.RegisterVideoSourceListener() {
          @Override
          public void addVideoSource(@NotNull String path, @NotNull String source) {
            entity.getService().addSource(path, source);
          }

          @Override
          public void removeVideoSource(@NotNull String path) {
            entity.getService().removeSource(path);
          }
        }));
      } else {
        for (Unregistered unregistered : providerUnregistered) {
          unregistered.unregister();
        }
        providerUnregistered.clear();
      }
    });
  }

  @Override
  public @NotNull URL getAddonImageURL() {
    return Objects.requireNonNull(getResource("images/image64.png"));
  }
}
