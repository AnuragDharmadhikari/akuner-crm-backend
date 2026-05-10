package org.ved.crm.ai.dto;

public record TerritoryNarrativeDto(
        String territoryId,
        String territoryName,
        String period,

        String narrative,
        String narrativeMr,

        String strengths,
        String strengthsMr,

        String concerns,
        String concernsMr,

        String recommendations,
        String recommendationsMr
) {}