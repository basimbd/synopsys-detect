package com.synopsys.integration.detectable.detectables.pipenv.build.parser;

import com.synopsys.integration.detectable.detectables.pipenv.build.model.PipFreeze;
import com.synopsys.integration.detectable.detectables.pipenv.build.model.PipFreezeEntry;

import java.util.List;
import java.util.stream.Collectors;

public class PipenvFreezeParser {
    private static final String VERSION_SEPARATOR = "==";

    public PipFreeze parse(List<String> pipFreezeOutput) {
        List<PipFreezeEntry> entries = pipFreezeOutput.stream()
            .map(line -> line.split(VERSION_SEPARATOR))
            .filter(pieces -> pieces.length == 2)
            .map(pieces -> new PipFreezeEntry(pieces[0], pieces[1]))
            .collect(Collectors.toList());

        return new PipFreeze(entries);
    }
}
