package com.synthtk.indifferent.api;

import android.content.Context;

import com.synthtk.indifferent.R;

/**
 * Created by Chris on 1/8/2015.
 */
public class Deal {
    String id;
    String features;
    String title;
    String specifications;
    String url;
    String soldOutAt;
    String[] photos;
    Item[] items;
    Topic topic;
    Theme theme;

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSpecifications() {
        return specifications;
    }

    public void setSpecifications(String specifications) {
        this.specifications = specifications;
    }

    public Item[] getItems() {
        return items;
    }

    public void setItems(Item[] items) {
        this.items = items;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSoldOutAt() {
        return soldOutAt;
    }

    public void setSoldOutAt(String soldOutAt) {
        this.soldOutAt = soldOutAt;
    }

    public String[] getPhotos() {
        return photos;
    }

    public void setPhotos(String[] photos) {
        this.photos = photos;
    }

    public String getPrices(Context context) {
        int low = -1;
        int high = -1;
        for (Item item : items) {
            int price = item.getPrice();
            if (price < low || low == -1) {
                low = price;
            }
            if (price > high || high == -1) {
                high = price;
            }
        }
        String price;
        if (low != high) {
            price = low + "-" + high;
        } else {
            price = String.valueOf(low);
        }
        return context.getString(R.string.deal_price, price);
    }

    public static class Item {
        String condition;
        String id;
        int price;
        String photo;
        Attribute[] attributes;

        public Attribute[] getAttributes() {
            return attributes;
        }

        public void setAttributes(Attribute[] attributes) {
            this.attributes = attributes;
        }

        public String getCondition() {
            return condition;
        }

        public void setCondition(String condition) {
            this.condition = condition;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getPrice() {
            return price;
        }

        public void setPrice(int price) {
            this.price = price;
        }

        public String getPhoto() {
            return photo;
        }

        public void setPhoto(String photo) {
            this.photo = photo;
        }

        public static class Attribute {
            String key;
            String value;

            public String getKey() {
                return key;
            }

            public void setKey(String key) {
                this.key = key;
            }
        }
    }

    public static class Story {
        String title;
        String body;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    public static class Theme {
        String accentColor;
        String backgroundColor;
        String backgroundImage;
        String foreground;

        public String getAccentColor() {
            return accentColor;
        }

        public void setAccentColor(String accentColor) {
            this.accentColor = accentColor;
        }

        public String getBackgroundColor() {
            return backgroundColor;
        }

        public void setBackgroundColor(String backgroundColor) {
            this.backgroundColor = backgroundColor;
        }

        public String getBackgroundImage() {
            return backgroundImage;
        }

        public void setBackgroundImage(String backgroundImage) {
            this.backgroundImage = backgroundImage;
        }

        public String getForeground() {
            return foreground;
        }

        public void setForeground(String foreground) {
            this.foreground = foreground;
        }
    }

}
