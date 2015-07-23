package weatherpony.util.streams;

import java.io.IOException;
import java.io.OutputStream;

public class TeeOutputStream extends OutputStream{
	public TeeOutputStream(OutputStream out1, OutputStream out2){
		super();
		this.out1 = out1;
		this.out2 = out2;
	}
	private final OutputStream out1, out2;
	@Override
	public void close() throws IOException{
		try{
			this.out1.close();
		}finally{
			this.out2.close();
		}
	}
	@Override
	public void flush() throws IOException{
		try{
			this.out1.flush();
		}finally{
			this.out2.flush();
		}
	}
	@Override
	public void write(int b) throws IOException{
		try{
			this.out1.write(b);
		}finally{
			this.out2.write(b);
		}
	}
}
