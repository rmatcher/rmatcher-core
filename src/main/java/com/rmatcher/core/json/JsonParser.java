package com.rmatcher.core.json;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import com.google.gson.Gson;

public class JsonParser<T> implements Iterable<T>{
    private BufferedReader br = null;
    private final Gson gson = new Gson();
    private Object object;

    JsonParser(Object object){
        this.object = Preconditions.checkNotNull(object);
    }

    public void createBuffer(String jsonFilePath){
        try {
            br = new BufferedReader(new FileReader(jsonFilePath));
        } catch (IOException e){
            throw new RuntimeException("Failed to create BufferReader for " + jsonFilePath);
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new AbstractIterator<T>() {
            protected T computeNext() {
                String jsonString;
                try {
                    if ((jsonString = br.readLine()) != null) {
                        Object o = gson.fromJson(jsonString, object.getClass());
                        if(o.getClass() == object.getClass()){
                            return (T)o;
                        } else{
                          throw new RuntimeException("Objects are not assignable");
                        }
                    }
                    return endOfData();
                } catch (IOException e){
                    throw new RuntimeException("Error while parsing the file");
                }
            }
        };
    }
}

