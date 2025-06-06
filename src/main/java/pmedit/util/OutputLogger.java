package pmedit.util;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.Level;
import ch.qos.logback.core.helpers.NOPAppender;

public class OutputLogger {
    public static Logger createFileLogger(String name, String filePath) {
        LoggerContext context = new LoggerContext();
        context.putProperty("CUSTOM_LEVEL_CONVERTER", CustomLevelConverter.class.getName());
        // Explicitly set MDC adapter
        context.setMDCAdapter(new LogbackMDCAdapter());
        context.start();

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("[%d{yyyy-MM-dd HH:mm:ss}] [%-5level] - %msg%n");
        encoder.start();

        FileAppender<ILoggingEvent> fileAppender =
                new FileAppender<>();
        fileAppender.setContext(context);
        fileAppender.setFile(filePath);
        fileAppender.setAppend(true);
        fileAppender.setEncoder(encoder);
        fileAppender.start();

        Logger logger = context.getLogger(name);
        logger.addAppender(fileAppender);
        logger.setLevel(Level.ALL);
        logger.setAdditive(false);

        return logger;
    }


    public static Logger createNullLogger() {
        LoggerContext context =  new LoggerContext();
        context.start();

        // Create a NOP (No Operation) appender
        NOPAppender<ILoggingEvent> nullAppender = new NOPAppender<>();
        nullAppender.setContext(context);
        nullAppender.start();

        Logger logger = context.getLogger("ignored");
        logger.addAppender(nullAppender);
        logger.setLevel(Level.ALL); // Accept all levels but discard them
        logger.setAdditive(false);

        return logger;
    }

    public static  class CustomLevelConverter extends ClassicConverter {

        @Override
        public String convert(ILoggingEvent event) {
            Level level = event.getLevel();

            switch (level.toInt()) {
                case Level.TRACE_INT, Level.DEBUG_INT, Level.INFO_INT: return "";
                case Level.WARN_INT:  return "WARN";
                case Level.ERROR_INT: return "ERROR";
                default: return level.toString() + ":";
            }
        }
    }


}
