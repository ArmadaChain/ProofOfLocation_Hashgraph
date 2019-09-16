pragma solidity 0.5.1;

contract ProofofLocation {

    struct ItemStruct {
        string long;
        string lat;
    }

    mapping(string => ItemStruct) itemStructs;
    string [] public itemList;

    event LogItemLoc(string item, string itemLong, string itemLat);

// location is stored in uint, needs to be put into decimal when read back
    function appendItemLoc(string memory item, string memory itemLong, string memory itemLat) public {
        itemList.push(item);
        itemStructs[item].long = itemLong;
        itemStructs[item].lat = itemLat;
    }

    function getItemCount() public view returns(uint count) {
        return itemList.length;
    }

    function itemLoop() public {

        for (uint i=0; i<itemList.length; i++) {
            emit LogItemLoc(itemList[i], itemStructs[itemList[i]].long, itemStructs[itemList[i]].lat);
        }
    }

    function getAddress(string memory item) view public returns (string memory, string memory) {
        return (itemStructs[item].long, itemStructs[item].lat);
    }
}