package com.valentinnikolaev.hibernatecrud.models;

import javax.persistence.*;
import java.util.Scanner;

@Entity
@Table(name = "regions")
public class Region {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long   id;

    @Column
    private String name;

    public Region() {
    }

    public Region(Long id, String name) {
        this.id   = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Region setName(String name) {
        this.name = name;
        return this;
    }

    public String getDataForSerialisation() {
        return "Region id:" + this.getId() + ";" + "Region name:" + this.getName() + ";";
    }

    public static Region parse(String regionData) {
        Long id;
        String name;

        Scanner scanner = new Scanner(regionData);
        scanner.useDelimiter(";");

        scanner.findInLine("Region id:");
        if (scanner.hasNextLong()) {
            id = scanner.nextLong();
        } else {
            throw new IllegalArgumentException("Can`t parse region id.");
        }

        scanner.findInLine("Region name:");
        if (scanner.hasNext()) {
            name = scanner.next();
        } else {
            throw new IllegalArgumentException("Can`t parse region name.");
        }

        return new Region(id, name);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        if (this.hashCode() != obj.hashCode()) {
            return false;
        }

        if (! (obj instanceof Region)) {
            return false;
        }

        Region comparingObj = (Region) obj;
        return this.name.equals(comparingObj.name);
    }

    @Override
    public String toString() {
        return "Region{" + "id=" + id + ", name='" + name + '\'' + '}';
    }
}
