package org.luizricardo.warppipe.fakes;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class FakeServletOutputStream extends ServletOutputStream {

    private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void write(int b) throws IOException {
        outputStream.write(b);
    }

    public ByteArrayOutputStream getOutput() {
        return outputStream;
    }
}
