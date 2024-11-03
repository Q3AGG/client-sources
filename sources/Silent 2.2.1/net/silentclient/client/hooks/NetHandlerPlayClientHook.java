package net.silentclient.client.hooks;

//#if MC==10809
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.client.C19PacketResourcePackStatus;
import net.minecraft.network.play.server.S48PacketResourcePackSend;
import net.silentclient.client.Client;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
//#endif

public class NetHandlerPlayClientHook {
    //#if MC==10809
    public static boolean validateResourcePackUrl(NetHandlerPlayClient client, S48PacketResourcePackSend packet) {
        try {
            String url = packet.getURL();
            final URI uri = new URI(url);
            final String scheme = uri.getScheme();
            final boolean isLevelProtocol = "level".equals(scheme);

            if (!"http".equals(scheme) && !"https".equals(scheme) && !isLevelProtocol) {
                client.getNetworkManager().sendPacket(new C19PacketResourcePackStatus(packet.getHash(), C19PacketResourcePackStatus.Action.FAILED_DOWNLOAD));
                throw new URISyntaxException(url, "Wrong protocol");
            }

            url = URLDecoder.decode(url.substring("level://".length()), StandardCharsets.UTF_8.toString());

            if (isLevelProtocol && (url.contains("..") || !url.endsWith("/resources.zip"))) {
                Client.logger.warn("Malicious server tried to access {}", url);

                throw new URISyntaxException(url, "Invalid levelstorage resourcepack path");
            }

            return true;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return false;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return false;
    }
    //#endif
}
