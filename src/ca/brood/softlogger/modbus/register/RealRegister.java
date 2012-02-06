package ca.brood.softlogger.modbus.register;

import ca.brood.softlogger.util.*;
import net.wimpi.modbus.msg.ModbusRequest;
import net.wimpi.modbus.msg.ReadCoilsRequest;
import net.wimpi.modbus.msg.ReadInputDiscretesRequest;
import net.wimpi.modbus.msg.ReadInputRegistersRequest;
import net.wimpi.modbus.msg.ReadMultipleRegistersRequest;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.*;

public class RealRegister extends Register implements Comparable<RealRegister>{
	protected int address = Integer.MAX_VALUE;
	protected int size = 0;
	protected int sizePerAddress = 2; //size of each address.  for 16-bit addresses, this is 2.  For coils, this is 1.  We know that the next contiguous register should be at this.address+(this.size/this.sizePerAddress)
	protected RegisterType regType;
	
	protected RealRegister() {
		super();
		log = Logger.getLogger(RealRegister.class);
	}
	public boolean configure(Node registerNode) {
		if (!super.configure(registerNode)) {
			return false;
		}
		NodeList configNodes = registerNode.getChildNodes();
		for (int i=0; i<configNodes.getLength(); i++) {
			Node configNode = configNodes.item(i);
			if (("#text".compareToIgnoreCase(configNode.getNodeName())==0))	{
				continue;
			} else if (("regAddr".compareToIgnoreCase(configNode.getNodeName())==0))	{
				int addy = 0;
				try {
					addy = Util.parseInt(configNode.getFirstChild().getNodeValue());
				} catch (NumberFormatException e) {
					log.error("Couldn't parse regAddr to integer from: "+configNode.getFirstChild().getNodeValue());
					return false;
				}
				try {
					this.regType = RegisterType.fromAddress(addy);
					this.address = RegisterType.getAddress(addy);
					registerNode.removeChild(configNode);
				} catch (NumberFormatException e) {
					log.error("Error converting modbuss address to register type: "+addy);
					return false;
				}
			} else if (("size".compareToIgnoreCase(configNode.getNodeName())==0))	{
				try {
					this.size = Util.parseInt(configNode.getFirstChild().getNodeValue());
					registerNode.removeChild(configNode);
				} catch (NumberFormatException e) {
					log.error("Couldn't parse size to integer from: "+configNode.getFirstChild().getNodeValue());
				}
			}
		}
		if (this.address < 0 || this.address > 65535) {
			log.error("Parsed invalid address: "+this.address);
			return false;
		}
		switch (this.regType) {
		case INPUT_COIL:
		case OUTPUT_COIL:
			this.sizePerAddress = 1;
			if (this.size != 1) {
				log.warn("Got invalid size for an input or output coil.  Changing size to 1 from: "+this.size);
				this.size = 1;
			}
			break;
		case INPUT_REGISTER:
		case OUTPUT_REGISTER:
			this.sizePerAddress = 2;
			if (this.size != 2 && this.size != 4) {
				log.warn("Got invalid size for an input or output register.  Changing size to 2 from: "+this.size);
				this.size = 2;
			}
			break;
		}
		return true;
	}

	//Performs an inline sort of the passed in array of registers by address.
	//Also arranges the registers into contiguous groups and returns them (not completed).
	public static ArrayList<ArrayList<? extends RealRegister>> organizeRegisters(ArrayList<? extends RealRegister> in) {
		//TODO: Finish this.  Need to group the registers together
		ArrayList<ArrayList<? extends RealRegister>> ret = new ArrayList<ArrayList<? extends RealRegister>>();
		Collections.sort(in);		
		return ret;
	}
	
	@Override
	public int compareTo(RealRegister other) {
		if (other.address == this.address)
			return 0;
		if (other.address < this.address)
			return 1;
		if (other.address > this.address)
			return -1;
		//should never get here
		return 0;
	}
	
	@Override
	public String toString() {
		return "RealRegister: fieldname="+this.fieldName+"; address="+this.address+"; size="+this.size+"; data: "+registerData.toString();
	}
	public ModbusRequest getRequest() {
		//TODO: What happens if this.size % this.address != 0?
		//TODO: Fix this to check what time of memory to read from
		ModbusRequest request = null;
		switch (this.regType) {
		case INPUT_COIL:
			request = new ReadInputDiscretesRequest(this.address, this.size/this.sizePerAddress);
			break;
		case OUTPUT_COIL:
			request = new ReadCoilsRequest(this.address, this.size/this.sizePerAddress);
			break;
		case INPUT_REGISTER:
			request = new ReadInputRegistersRequest(this.address, this.size/this.sizePerAddress);
			break;
		case OUTPUT_REGISTER:
			request = new ReadMultipleRegistersRequest(this.address, this.size/this.sizePerAddress);
		}
		
		return request;
	}
}
