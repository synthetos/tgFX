/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tgfx.updater.firmware;

/**
 *
 * @author rileyporter
 */
public class ChipData {
    
    private int PageSize;
    private int NRWWSize;
    private int TotalMemory;
    
    
    public ChipData(int PageSize, int NRWWSize, int NumberOfPages){
        this.NRWWSize = NRWWSize;
        this.PageSize = PageSize;
        this.TotalMemory = NumberOfPages * PageSize * 2;

    }
    
    
    
}
