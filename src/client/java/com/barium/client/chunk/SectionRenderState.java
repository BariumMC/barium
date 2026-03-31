package com.barium.client.chunk;

public final class SectionRenderState {
    private final int yIndex;
    private boolean empty;
    private boolean visible;
    private boolean inVerticalRange;
    private boolean needsMeshUpdate;

    public SectionRenderState(int yIndex, boolean empty) {
        this.yIndex = yIndex;
        this.empty = empty;
        this.visible = !empty;
        this.inVerticalRange = !empty;
        this.needsMeshUpdate = !empty;
    }

    public int yIndex() {
        return yIndex;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
        if (empty) {
            this.visible = false;
            this.inVerticalRange = false;
            this.needsMeshUpdate = false;
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isInVerticalRange() {
        return inVerticalRange;
    }

    public void setInVerticalRange(boolean inVerticalRange) {
        this.inVerticalRange = inVerticalRange;
    }

    public boolean needsMeshUpdate() {
        return needsMeshUpdate;
    }

    public void setNeedsMeshUpdate(boolean needsMeshUpdate) {
        this.needsMeshUpdate = needsMeshUpdate;
    }
}
