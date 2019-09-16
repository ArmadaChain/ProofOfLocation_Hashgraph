pragma solidity 0.5.1;
//Simple Proof of Location Contract
//Accomodate limited smart contract storage, for more effective contract to store multiple values refer to ProofofLocation.sol

contract ProofofLocationSimple {
  address shipee_address;
  string [] public itemList;
  string private latitude;
  string private longitude;

  constructor() public {
        // msg provides details about the message that's sent to the contract
        // msg.sender is contract caller (address of contract creator)
        shipee_address = msg.sender;
    }

  function long_return() view public returns (string memory) {return longitude; }

  function lat_return() view public returns (string memory) {return latitude; }

  function set_long(string memory new_longitude) public { longitude = new_longitude; }

  function set_lat(string memory new_latitude) public { latitude = new_latitude; }
}