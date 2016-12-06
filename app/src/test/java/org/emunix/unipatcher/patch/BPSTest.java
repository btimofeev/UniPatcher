package org.emunix.unipatcher.patch;

import android.content.Context;

import org.apache.commons.io.FileUtils;
import org.emunix.unipatcher.R;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class BPSTest {

    private static final String PATCH_CORRUPTED = "The patch file is corrupted.";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Mock
    Context mockContext;

    @Before
    public void setUp() throws Exception {
        when(mockContext.getString(R.string.notify_error_patch_corrupted))
                .thenReturn(PATCH_CORRUPTED);
    }

    @Test
    public void testApply() throws Exception {
        assertTrue(ApplyPatch("/bps/1.bps", "/bps/1.bin", "/bps/1m.bin"));
    }

    private boolean ApplyPatch(String patchName, String origName, String modifiedName) throws Exception {
        File patch = new File(this.getClass().getResource(patchName).getPath());
        File in = new File(getClass().getResource(origName).getPath());
        File out = folder.newFile("out.bin");

        BPS patcher = new BPS(mockContext, patch, in, out);
        try {
            patcher.apply();
        } catch (PatchException | IOException e) {
            fail("Patching failed");
        }

        File origOut = new File(getClass().getResource(modifiedName).getPath());
        return FileUtils.checksumCRC32(out) == FileUtils.checksumCRC32(origOut);
    }
}