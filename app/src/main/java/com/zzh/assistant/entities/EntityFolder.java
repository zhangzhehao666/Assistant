package com.zzh.assistant.entities;

import java.util.List;

public class EntityFolder {
    public String name;
    public String path;
    public EntityImage cover;
    public List<EntityImage> images;

    @Override
    public boolean equals(Object o) {
        try {
            EntityFolder other = (EntityFolder) o;
            return this.path.equalsIgnoreCase(other.path);
        }catch (ClassCastException e){
            e.printStackTrace();
        }
        return super.equals(o);
    }
}
