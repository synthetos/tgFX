/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.updater.firmware;

/**
 *
 * @author rileyporter
 */
public class Avr109Protocol {
    
    
    private enum AVRProgCommands{
        ENTER_PROGRAMMING_MODE,
        AUTO_INCREMENT_ADDRESS,
        SET_ADDRESS,
        WRITE_PROGRAM_MEMEORY_LOW_BYTE,
        WRITE_PROGRAM_MEMORY_HIGH_BYTE,
        ISSUE_PAGE_WRITE,
        READ_LOCK_BITS,
        READ_PROGRAM_MEMORY,
        READ_DATA_MEMORY,
        WRITE_DATA_MEMORY,
        CHIP_ERASE,
        WRITE_LOCK_BITS,
        READ_FUSE_BITS,
        READ_HIGH_FUSE_BITS,
        READ_EXTENDED_FUSE_BITS,
        LEAVE_PROGRAMMING_MODE,
        SELECT_DEVICE_TYPE,
        READ_SIGNATURE_BYTES,
        RETURN_SUPPORTED_DEVICE_CODES,
        RETURN_SOFTWARE_IDENTIFIER,
        RETURN_SOFTWARE_VERSION,
        RETURN_PROGRAMMER_TYPE,
        SET_LED,
        CLEAR_LED,
        EXIT_BOOTLOADER,
        CHECK_BLOCK_SUPPORT,
        START_BLOCK_FLASH_LOAD,
        START_BLOCK_EEPROM_LOAD,
        START_BLOCK_FLASH_READ,
        START_BLOCK_EERPOM_READ
    }
}
