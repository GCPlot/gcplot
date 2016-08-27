package com.gcplot.web;

import java.io.File;

public interface UploadedFile {

    String originalName();

    String fileName();

    String contentType();

    long size();

    File file();

}
