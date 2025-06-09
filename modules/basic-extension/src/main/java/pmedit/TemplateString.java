package pmedit;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TemplateString {
    int length;

    String template;
    List<Entity> entityList;
    Pattern extractPattern;

    public TemplateString(String template, int maxOutputLenght) {
        length = maxOutputLenght;
        this.template = template;
    }
    public TemplateString(String template) {
        this(template, 0xFFFFFFF);
    }

    public void parse() {
        entityList = new ArrayList<Entity>();
        if (template == null)
            return;
        int idx = 0;
        while (true) {
            int openIdx = template.indexOf("{", idx);
            if (openIdx > 0)
                entityList.add(new Literal(template.substring(idx, openIdx)));
            if (openIdx >= 0) {
                int closeIdx = template.indexOf("}", openIdx);
                if (closeIdx >= 0) {
                    String varName = template.substring(openIdx + 1, closeIdx);
                    entityList.add(new Variable(varName));
                    idx = closeIdx + 1;
                } else {
                    entityList.add(new Literal(template.substring(openIdx)));
                    break;
                }
            } else {
                entityList.add(new Literal(template.substring(idx)));
                break;
            }
        }
    }

    public String process(MetadataInfo md) {
        return process(md, null);
    }

    public String process(MetadataInfo md, BiFunction<Entity, String, String> extraProcessor) {
        if (entityList == null)
            parse();
        ArrayList<String> chunks = new ArrayList<String>();
        ArrayList<Integer> resizable = new ArrayList<Integer>();
        int outSize = 0;
        for (int i = 0; i < entityList.size(); ++i) {
            Entity e = entityList.get(i);
            String value = e.get(md);
            if(extraProcessor != null) {
                value = extraProcessor.apply(e, value);
                if(value == null) {
                    value = "";
                }
            }
            chunks.add(value);
            if (e.shrinkable() && value.length() > 0)
                resizable.add(i);
            outSize += value.length();
        }
        if (outSize > length && resizable.size() > 0) {
            int shirinkableSize = 0;
            for (Integer i : resizable) {
                shirinkableSize += chunks.get(i).length();
            }
            float[] shrinkCoef = new float[chunks.size()];
            for (Integer i : resizable) {
                shrinkCoef[i] = ((float) chunks.get(i).length()) / shirinkableSize;
            }
            for (Integer i : resizable) {
                String v = chunks.get(i);
                int reduceBy = Math.round(shrinkCoef[i] * (outSize - length));
                int endIndex = v.length() - reduceBy;
                if (endIndex > 0)
                    chunks.set(i, v.substring(0, endIndex));
                else
                    chunks.set(i, "");
            }
        }
        StringBuilder result = new StringBuilder();
        for (String chunk : chunks) {
            result.append(chunk);
        }
        return result.toString();
    }

    public MetadataInfo extract(String string) {
        if (extractPattern == null) {
            if(entityList == null) {
                parse();
            }
            StringBuilder sb = new StringBuilder();
            for (Entity e : entityList) {
                sb.append(e.asRegex());
            }
            extractPattern = Pattern.compile(sb.toString());
        }
        MetadataInfo md = new MetadataInfo();
        md.setEnabled(false);
        Matcher m = extractPattern.matcher(string);
        if(m.matches()){
            for (Entity e : entityList) {
                String groupName = e.groupName();
                if(groupName != null){
                    md.setFromString(e.name(), m.group(e.groupName()));
                    md.setEnabled(e.name(), true);
                }
            }
        }

        return md;
    }

    public interface Entity {
        String get(MetadataInfo md);

        boolean shrinkable();

        String asRegex();

        String name();
        String groupName();
    }

    public static class Variable implements Entity {
        String name;
        boolean nonGreedy;

        public Variable(String name) {
            if(name.endsWith("?")){
                nonGreedy = true;
                name = name.substring(0, name.length()-1);
            }
            this.name = name;
        }

        @Override
        public String get(MetadataInfo md) {
            return md.getString(name, "");
        }

        @Override
        public boolean shrinkable() {
            return true;
        }

        @Override
        public String asRegex() {
            return "(?<"+ groupName() +">.*" + (nonGreedy ? "?" : "") +")";
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String groupName() {
            return name.replace('.', '0');
        }

    }

    public static class Literal implements Entity {
        String literal;

        public Literal(String literal) {
            this.literal = literal;
        }

        @Override
        public String get(MetadataInfo md) {
            return literal;
        }

        @Override
        public boolean shrinkable() {
            return false;
        }

        @Override
        public String asRegex() {
            return literal.isEmpty() ? literal : Pattern.quote(literal);
        }

        @Override
        public String name() {
            return null;
        }

        @Override
        public String groupName() {
            return null;
        }
    }

}
