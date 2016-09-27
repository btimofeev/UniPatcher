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
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UPSTest {

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
        assertTrue(ApplyPatch("/ups/readUpsCrc.ups", "/ups/readUpsCrc.bin", "/ups/readUpsCrc_m.bin"));
    }

    @Test
    public void testReadUpsCrc() throws Exception {
        File patch = new File(this.getClass().getResource("/ups/readUpsCrc.ups").getPath());
        UPS.UpsCrc pCrc = null;
        try {
            pCrc = UPS.readUpsCrc(mockContext, patch);
        } catch (PatchException e) {
            fail("Patch exception");
        }
        assertEquals(pCrc.getPatchFileCRC(), pCrc.getRealPatchCRC());
    }

    @Test
    public void testCheckMagic() throws Exception {
        File patch = new File(this.getClass().getResource("/ups/readUpsCrc.ups").getPath());
        assertTrue(UPS.checkMagic(patch));
        File noPatch = new File(this.getClass().getResource("/ups/readUpsCrc.bin").getPath());
        assertFalse(UPS.checkMagic(noPatch));
    }

    private boolean ApplyPatch(String patchName, String origName, String modifiedName) throws Exception {
        File patch = new File(this.getClass().getResource(patchName).getPath());
        File in = new File(getClass().getResource(origName).getPath());
        File out = folder.newFile("out.bin");

        UPS patcher = new UPS(mockContext, patch, in, out);
        try {
            patcher.apply();
        } catch (PatchException | IOException e) {
            fail("Patching failed");
        }

        File origOut = new File(getClass().getResource(modifiedName).getPath());
        return FileUtils.checksumCRC32(out) == FileUtils.checksumCRC32(origOut);
    }
}