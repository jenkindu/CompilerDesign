package comp6421.semantic.value;

import comp6421.semantic.CompilerError;
import comp6421.semantic.InternalCompilerError;
import comp6421.semantic.code.AddWordInstruction;
import comp6421.semantic.code.CodeGenerationContext;
import comp6421.semantic.code.Register;

public class StoredValue extends DynamicValue {

	private final Value offset;
	private final Value baseAddress;

	public StoredValue(Value baseAddr, Value offset){
		this.baseAddress = baseAddr;
		this.offset      = offset;
	}

	public Value getOffset() {
		return offset;
	}

	public Value getBaseAddress() {
		return baseAddress;
	}
	
	@Override
	public String toString() {
		return "*(" + baseAddress + " + " + offset + ")";
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof StoredValue 
				&& ((StoredValue)other).getBaseAddress().equals(baseAddress) 
				&& ((StoredValue)other).getOffset().equals(offset); 
	}

	@Override
	public Value getUseableValue(CodeGenerationContext c) throws CompilerError {
		return getConcreteAddress(c).getUseableValue(c);
	}

	@Override
	public RegisterValue getRegisterValue(CodeGenerationContext c) throws CompilerError {
		return getConcreteAddress(c).getRegisterValue(c);
	}
	
	public ConcreteAddressValue	getConcreteAddress(CodeGenerationContext c) throws CompilerError{
		Value useableOffset = offset.getUseableValue(c);
		RegisterValue baseAddrReg = baseAddress.getRegisterValue(c);

		if(useableOffset instanceof StaticValue){
			
			return new ConcreteAddressValue(baseAddrReg, (StaticValue) useableOffset);
			
		}else
		if(useableOffset instanceof RegisterValue){

			RegisterValue tempReg = new RegisterValue(c.getTemporaryRegister(baseAddrReg.getRegister()));
			
			c.appendInstruction(new AddWordInstruction(tempReg, baseAddrReg, (RegisterValue)useableOffset));

			Register useableOffsetRegister = ((RegisterValue) useableOffset).getRegister();
			if( ! useableOffsetRegister.reserved){
				c.freeTemporaryRegister(useableOffsetRegister);				
			}
			
			return new ConcreteAddressValue(tempReg, new StaticIntValue(0));
						
		}else{
			throw new InternalCompilerError("getUseableValue for offset returned an instance of " + useableOffset.getClass().getName());
		}
		
	}
	
}
