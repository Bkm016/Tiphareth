package ink.ptms.tiphareth;

import ink.ptms.tiphareth.pack.PackDispatcher;
import ink.ptms.tiphareth.pack.PackGenerator;
import ink.ptms.tiphareth.pack.PackLoader;
import ink.ptms.tiphareth.pack.PackUploader;

public interface TipharethAPI {

    PackLoader LOADER = PackLoader.INSTANCE;

    PackUploader UPLOADER = PackUploader.INSTANCE;

    PackGenerator GENERATOR = PackGenerator.INSTANCE;

    PackDispatcher DISPATCHER = PackDispatcher.INSTANCE;

}
