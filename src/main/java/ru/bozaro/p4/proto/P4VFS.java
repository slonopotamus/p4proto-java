package ru.bozaro.p4.proto;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * @author Marat Radchenko
 */
public interface P4VFS {

    @NotNull
    FileStatus getFileStatus(@NotNull String path);

    @NotNull
    InputStream openFile(@NotNull String path) throws IOException;

    enum FileStatus {
        Exists,
        Missing;

        @NotNull
        String toP4() {
            return name().toLowerCase(Locale.ENGLISH);
        }
    }
}
