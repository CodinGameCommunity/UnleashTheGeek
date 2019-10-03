package com.codingame.view;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.codingame.game.Referee;
import com.codingame.gameengine.core.AbstractPlayer;
import com.codingame.gameengine.core.GameManager;
import com.codingame.gameengine.core.Module;
import com.codingame.utg2019.Coord;
import com.google.inject.Inject;

public class ViewModule implements Module {

    private GameManager<AbstractPlayer> gameManager;
    private Referee referee;

    @Inject
    ViewModule(GameManager<AbstractPlayer> gameManager) {
        this.gameManager = gameManager;
        gameManager.registerModule(this);
    }

    public void setReferee(Referee referee) {
        this.referee = referee;
    }

    @Override
    public final void onGameInit() {
        sendGlobalData();
        sendFrameData();
    }

    private void sendFrameData() {
        FrameViewData data = referee.getCurrentFrameData();
        gameManager.setViewData("graphics", serialize(data));
    }

    private void sendGlobalData() {
        GlobalViewData data = referee.getGlobalData();
        gameManager.setViewGlobalData("graphics", serialize(data));

    }

    @Override
    public final void onAfterGameTurn() {
        sendFrameData();
    }

    @Override
    public final void onAfterOnEnd() {
        sendFrameData();
    }

    private String serialize(GlobalViewData data) {
        List<String> lines = new ArrayList<>();
        lines.add(Referee.join(data.width, data.height, data.agentsPerPlayer, data.ore.size()));

        data.ore.stream()
            .forEach(ore -> {
                lines.add(Referee.join(ore.x, ore.y, ore.ore));
            });

        return lines.stream().collect(Collectors.joining("\n"));
    }

    private String serialize(FrameViewData data) {
        List<String> lines = new ArrayList<>();
        lines.add(Referee.join(serialize(data.scores), data.events.size()));
        data.agents.stream()
            .sorted((a, b) -> a.id - b.id)
            .forEach(agent -> {
                lines.add(Referee.join(agent.id, agent.x, agent.y, agent.item, agent.dead ? 1 : 0));
                lines.add(agent.message == null ? "" : agent.message);
                if (agent.tx != null) {
                    lines.add(Referee.join(agent.tx, agent.ty));
                }
            });
        data.events.stream()
            .forEach(event -> {
                lines
                    .add(
                        Referee.join(
                            event.type,
                            coalesce(event.item, "_"),
                            coalesce(event.agent, "_"),
                            coalesce(event.x, "_"),
                            coalesce(event.y, "_")
                        )
                    );
            });
        return lines.stream().collect(Collectors.joining("\n"));
    }

    private String coalesce(Integer item, String string) {
        return item == null ? string : String.valueOf(item);
    }

    private String serialize(List<Integer> scores) {
        return scores.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(" "));
    }

}
