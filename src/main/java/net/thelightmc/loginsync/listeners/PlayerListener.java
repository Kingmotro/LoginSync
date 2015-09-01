package net.thelightmc.loginsync.listeners;

import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;
import net.thelightmc.loginsync.LoginSync;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.UUID;

public class PlayerListener implements Listener {
    private final JedisPool jedisPool;
    private final LoginSync loginSync;
    private final String denyReason;

    public PlayerListener(LoginSync loginSync) {
        this.loginSync = loginSync;
        Configuration config = loginSync.getConfig();
        this.denyReason = config.getString("denyReason");
        this.jedisPool = new JedisPool(config.getString("host"), config.getInt("port"));
    }

    @EventHandler
    public void onPlayerLogin(LoginEvent event) {
        event.registerIntent(loginSync);
        loginSync.getProxy().getScheduler().runAsync(loginSync, () -> {
            UUID uniqueId = event.getConnection().getUniqueId();
            Jedis jedis = jedisPool.getResource();
            if (jedis.sismember("loggedIn",uniqueId.toString())) {
                event.setCancelled(true);
                event.setCancelReason(denyReason);
                event.completeIntent(loginSync);
                return;
            }
            jedis.sadd("loggedIn", uniqueId.toString());
            event.completeIntent(loginSync);
        });
    }

    @EventHandler
    public void onProxyPing(ProxyPingEvent event) {
        event.registerIntent(loginSync);
        ServerPing.Players players = event.getResponse().getPlayers();
        loginSync.getProxy().getScheduler().runAsync(loginSync, () -> players.setOnline(jedisPool.getResource().scard("loggedIn").intValue()));
    }

    @EventHandler
    public void onPlayerQuit(PlayerDisconnectEvent event) {
        loginSync.getProxy().getScheduler().runAsync(loginSync, () -> jedisPool.getResource()
                        .srem("loggedIn", event.getPlayer().getUniqueId().toString()));
    }
}
