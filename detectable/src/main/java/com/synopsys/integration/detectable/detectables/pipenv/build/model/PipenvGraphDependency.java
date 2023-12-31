package com.synopsys.integration.detectable.detectables.pipenv.build.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PipenvGraphDependency {
    @SerializedName("package_name")
    private final String name;
    @SerializedName("installed_version")
    private final String installedVersion;
    @SerializedName("dependencies")
    private final List<PipenvGraphDependency> children;

    public PipenvGraphDependency(String name, String installedVersion, List<PipenvGraphDependency> children) {
        this.name = name;
        this.installedVersion = installedVersion;
        this.children = children;
    }

    public String getName() {
        return name;
    }

    public String getInstalledVersion() {
        return installedVersion;
    }

    public List<PipenvGraphDependency> getChildren() {
        return children;
    }
}
